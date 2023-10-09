/*
 * Copyright (c) 2003, 2022, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package com.sun.management.internal;

import java.util.concurrent.TimeUnit;
import java.util.function.DoubleSupplier;
import java.util.function.LongSupplier;
import java.util.function.ToDoubleFunction;

import jdk.internal.platform.Metrics;
import sun.management.BaseOperatingSystemImpl;
import sun.management.VMManagement;
/**
 * Implementation class for the operating system.
 * Standard and committed hotspot-specific metrics if any.
 *
 * ManagementFactory.getOperatingSystemMXBean() returns an instance
 * of this class.
 */
class OperatingSystemImpl extends BaseOperatingSystemImpl
    implements com.sun.management.UnixOperatingSystemMXBean {

    private static final int MAX_ATTEMPTS_NUMBER = 10;
    private final Metrics containerMetrics;
    private ContainerCpuTicks systemLoadTicks = new SystemCpuTicks();
    private ContainerCpuTicks processLoadTicks = new ProcessCpuTicks();

    private abstract class ContainerCpuTicks {
        private volatile long usageTicks;
        private volatile long totalTicks;

        private double getUsageDividesTotal(long usageTicks, long totalTicks) {
            // If cpu quota or cpu shares are in effect. Calculate the cpu load
            // based on the following formula (similar to how
            // getCpuLoad0() is being calculated):
            //
            //   | usageTicks - usageTicks' |
            //  ------------------------------
            //   | totalTicks - totalTicks' |
            //
            // where usageTicks' and totalTicks' are historical values
            // retrieved via an earlier call of this method.
            if (usageTicks < 0 || totalTicks <= 0) {
                return -1;
            }
            long distance = usageTicks - this.usageTicks;
            this.usageTicks = usageTicks;
            long totalDistance = totalTicks - this.totalTicks;
            this.totalTicks = totalTicks;
            double systemLoad = 0.0;
            if (distance > 0 && totalDistance > 0) {
                systemLoad = ((double)distance) / totalDistance;
            }
            // Ensure the return value is in the range 0.0 -> 1.0
            systemLoad = Math.max(0.0, systemLoad);
            systemLoad = Math.min(1.0, systemLoad);
            return systemLoad;
        }

        public double getContainerCpuLoad() {
            assert(containerMetrics != null);
            long quota = containerMetrics.getCpuQuota();
            long share = containerMetrics.getCpuShares();
            if (quota > 0) {
                long numPeriods = containerMetrics.getCpuNumPeriods();
                long quotaNanos = TimeUnit.MICROSECONDS.toNanos(quota * numPeriods);
                return getUsageDividesTotal(cpuUsageSupplier().getAsLong(), quotaNanos);
            } else {
                // If CPU quotas are not active then find the average load for
                // all online CPUs that are allowed to run this container.

                // If the cpuset is the same as the host's one there is no need to iterate over each CPU
                if (isCpuSetSameAsHostCpuSet()) {
                    return defaultCpuLoadSupplier().getAsDouble();
                } else {
                    int[] cpuSet = containerMetrics.getEffectiveCpuSetCpus();
                    // in case the effectiveCPUSetCpus are not available, attempt to use just cpusets.cpus
                    if (cpuSet == null || cpuSet.length <= 0) {
                        cpuSet = containerMetrics.getCpuSetCpus();
                    }
                    if (cpuSet == null) {
                        // cgroups is mounted, but CPU resource is not limited.
                        // We can assume the VM is run on the host CPUs.
                        return defaultCpuLoadSupplier().getAsDouble();
                    } else if (cpuSet.length > 0) {
                        return cpuSetCalc().applyAsDouble(cpuSet);
                    }
                    return -1;
                }
            }
        }

        protected abstract DoubleSupplier defaultCpuLoadSupplier();
        protected abstract ToDoubleFunction<int[]> cpuSetCalc();
        protected abstract LongSupplier cpuUsageSupplier();
    }

    private class ProcessCpuTicks extends ContainerCpuTicks {

        @Override
        protected DoubleSupplier defaultCpuLoadSupplier() {
            return () -> getProcessCpuLoad0();
        }

        @Override
        protected ToDoubleFunction<int[]> cpuSetCalc() {
            return (int[] cpuSet) -> {
                int totalCPUs = getHostOnlineCpuCount0();
                int containerCPUs = getAvailableProcessors();
                return Math.min(1.0, getProcessCpuLoad0() * totalCPUs / containerCPUs);
            };
        }

        @Override
        protected LongSupplier cpuUsageSupplier() {
            return () ->  getProcessCpuTime();
        }

    }

    private class SystemCpuTicks extends ContainerCpuTicks {

        @Override
        protected DoubleSupplier defaultCpuLoadSupplier() {
            return () -> getCpuLoad0();
        }

        @Override
        protected ToDoubleFunction<int[]> cpuSetCalc() {
            return (int[] cpuSet) -> {
                double systemLoad = 0.0;
                for (int cpu : cpuSet) {
                    double cpuLoad = getSingleCpuLoad0(cpu);
                    if (cpuLoad < 0) {
                        return -1;
                    }
                    systemLoad += cpuLoad;
                }
                return systemLoad / cpuSet.length;
            };
        }

        @Override
        protected LongSupplier cpuUsageSupplier() {
            return () -> containerMetrics.getCpuUsage();
        }

    }

    OperatingSystemImpl(VMManagement vm) {
        super(vm);
        this.containerMetrics = jdk.internal.platform.Container.metrics();
    }

    public long getCommittedVirtualMemorySize() {
        return getCommittedVirtualMemorySize0();
    }

    public long getTotalSwapSpaceSize() {
        if (containerMetrics != null) {
            long limit = containerMetrics.getMemoryAndSwapLimit();
            // The memory limit metrics is not available if JVM runs on Linux host (not in a docker container)
            // or if a docker container was started without specifying a memory limit (without '--memory='
            // Docker option). In latter case there is no limit on how much memory the container can use and
            // it can use as much memory as the host's OS allows.
            long memLimit = containerMetrics.getMemoryLimit();
            if (limit >= 0 && memLimit >= 0) {
                return limit - memLimit; // might potentially be 0 for limit == memLimit
            }
        }
        return getTotalSwapSpaceSize0();
    }

    public long getFreeSwapSpaceSize() {
        if (containerMetrics != null) {
            long memSwapLimit = containerMetrics.getMemoryAndSwapLimit();
            long memLimit = containerMetrics.getMemoryLimit();
            if (memSwapLimit >= 0 && memLimit >= 0) {
                long deltaLimit = memSwapLimit - memLimit;
                // Return 0 when memSwapLimit == memLimit, which means no swap space is allowed.
                // And the same for memSwapLimit < memLimit.
                if (deltaLimit <= 0) {
                    return 0;
                }
                for (int attempt = 0; attempt < MAX_ATTEMPTS_NUMBER; attempt++) {
                    long memSwapUsage = containerMetrics.getMemoryAndSwapUsage();
                    long memUsage = containerMetrics.getMemoryUsage();
                    if (memSwapUsage > 0 && memUsage > 0) {
                        // We read "memory usage" and "memory and swap usage" not atomically,
                        // and it's possible to get the negative value when subtracting these two.
                        // If this happens just retry the loop for a few iterations.
                        long deltaUsage = memSwapUsage - memUsage;
                        if (deltaUsage >= 0) {
                            long freeSwap = deltaLimit - deltaUsage;
                            if (freeSwap >= 0) {
                                return freeSwap;
                            }
                        }
                    }
                }
            }
        }
        return getFreeSwapSpaceSize0();
    }

    public long getProcessCpuTime() {
        return getProcessCpuTime0();
    }

    public long getFreeMemorySize() {
        if (containerMetrics != null) {
            long usage = containerMetrics.getMemoryUsage();
            long limit = containerMetrics.getMemoryLimit();
            if (usage > 0 && limit >= 0) {
                return limit - usage;
            }
        }
        return getFreeMemorySize0();
    }

    public long getTotalMemorySize() {
        if (containerMetrics != null) {
            long limit = containerMetrics.getMemoryLimit();
            if (limit >= 0) {
                return limit;
            }
        }
        return getTotalMemorySize0();
    }

    public long getOpenFileDescriptorCount() {
        return getOpenFileDescriptorCount0();
    }

    public long getMaxFileDescriptorCount() {
        return getMaxFileDescriptorCount0();
    }

    public double getCpuLoad() {
        if (containerMetrics != null) {
            return systemLoadTicks.getContainerCpuLoad();
        }
        return getCpuLoad0();
    }

    public double getProcessCpuLoad() {
        if (containerMetrics != null) {
            return processLoadTicks.getContainerCpuLoad();
        }
        return getProcessCpuLoad0();
    }

    private boolean isCpuSetSameAsHostCpuSet() {
        if (containerMetrics != null && containerMetrics.getCpuSetCpus() != null) {
            return containerMetrics.getCpuSetCpus().length == getHostOnlineCpuCount0();
        }
        return false;
    }

    /* native methods */
    private native long getCommittedVirtualMemorySize0();
    private native long getFreeMemorySize0();
    private native long getFreeSwapSpaceSize0();
    private native long getMaxFileDescriptorCount0();
    private native long getOpenFileDescriptorCount0();
    private native long getProcessCpuTime0();
    private native double getProcessCpuLoad0();
    private native double getCpuLoad0();
    private native long getTotalMemorySize0();
    private native long getTotalSwapSpaceSize0();
    private native double getSingleCpuLoad0(int cpuNum);
    private native int getHostConfiguredCpuCount0();
    private native int getHostOnlineCpuCount0();
    // CPU ticks since boot in nanoseconds
    private native long getHostTotalCpuTicks0();

    static {
        initialize0();
    }

    private static native void initialize0();
}
