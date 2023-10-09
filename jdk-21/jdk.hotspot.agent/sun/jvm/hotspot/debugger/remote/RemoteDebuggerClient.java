/*
 * Copyright (c) 2002, 2022, Oracle and/or its affiliates. All rights reserved.
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
 */

package sun.jvm.hotspot.debugger.remote;

import java.rmi.*;
import java.util.*;
import java.lang.reflect.*;

import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.debugger.cdbg.*;
import sun.jvm.hotspot.debugger.remote.x86.*;
import sun.jvm.hotspot.debugger.remote.amd64.*;
import sun.jvm.hotspot.debugger.remote.ppc64.*;

/** An implementation of Debugger which wraps a
    RemoteDebugger, providing remote debugging via RMI.
    This implementation provides caching of the remote process's
    address space on the local machine where the user interface is
    running. */

public class RemoteDebuggerClient extends DebuggerBase implements JVMDebugger {
  private RemoteDebugger remoteDebugger;
  private RemoteThreadFactory threadFactory;
  private static final int cacheSize = 256 * 1024 * 1024; // 256 MB

  public RemoteDebuggerClient(RemoteDebugger remoteDebugger) throws DebuggerException {
    super();
    try {
      this.remoteDebugger = remoteDebugger;
      this.machDesc = remoteDebugger.getMachineDescription();
      utils = new DebuggerUtilities(machDesc.getAddressSize(), machDesc.isBigEndian(),
                                    machDesc.supports32bitAlignmentOf64bitTypes());
      int cachePageSize = 4096;
      int cacheNumPages = parseCacheNumPagesProperty(cacheSize / cachePageSize);
      String cpu = remoteDebugger.getCPU();
      if (cpu.equals("x86")) {
        threadFactory = new RemoteX86ThreadFactory(this);
      } else if (cpu.equals("amd64") || cpu.equals("x86_64")) {
        threadFactory = new RemoteAMD64ThreadFactory(this);
      } else if (cpu.equals("ppc64")) {
        threadFactory = new RemotePPC64ThreadFactory(this);
      } else {
        try {
          Class tf = Class.forName("sun.jvm.hotspot.debugger.remote." +
            cpu.toLowerCase() + ".Remote" + cpu.toUpperCase() +
            "ThreadFactory");
          Constructor[] ctf = tf.getConstructors();
          threadFactory = (RemoteThreadFactory)ctf[0].newInstance(this);
        } catch (Exception e) {
          throw new DebuggerException("Thread access for CPU architecture " + cpu + " not yet supported");
        }
      }

      // Cache portion of the remote process's address space.
      initCache(cachePageSize, cacheNumPages);

      jbooleanSize = remoteDebugger.getJBooleanSize();
      jbyteSize    = remoteDebugger.getJByteSize();
      jcharSize    = remoteDebugger.getJCharSize();
      jdoubleSize  = remoteDebugger.getJDoubleSize();
      jfloatSize   = remoteDebugger.getJFloatSize();
      jintSize     = remoteDebugger.getJIntSize();
      jlongSize    = remoteDebugger.getJLongSize();
      jshortSize   = remoteDebugger.getJShortSize();
      javaPrimitiveTypesConfigured = true;
      narrowOopBase  = remoteDebugger.getNarrowOopBase();
      narrowOopShift = remoteDebugger.getNarrowOopShift();
      narrowKlassBase  = remoteDebugger.getNarrowKlassBase();
      narrowKlassShift = remoteDebugger.getNarrowKlassShift();
      heapOopSize  = remoteDebugger.getHeapOopSize();
      klassPtrSize  = remoteDebugger.getKlassPtrSize();
    }
    catch (RemoteException e) {
      throw new DebuggerException(e);
    }
  }

  public long[] getThreadIntegerRegisterSet(Address addr) {
    try {
      return remoteDebugger.getThreadIntegerRegisterSet(getAddressValue(addr), true);
    }
    catch (RemoteException e) {
      throw new DebuggerException(e);
    }
  }

  public long[] getThreadIntegerRegisterSet(long id) {
    try {
      return remoteDebugger.getThreadIntegerRegisterSet(id, false);
    }
    catch (RemoteException e) {
      throw new DebuggerException(e);
    }
  }

  /** Unimplemented in this class (remote remoteDebugger should already be attached) */
  public boolean hasProcessList() throws DebuggerException {
    throw new DebuggerException("Should not be called on RemoteDebuggerClient");
  }

  /** Unimplemented in this class (remote remoteDebugger should already be attached) */
  public List<ProcessInfo> getProcessList() throws DebuggerException {
    throw new DebuggerException("Should not be called on RemoteDebuggerClient");
  }

  /** Unimplemented in this class (remote remoteDebugger should already be attached) */
  public void attach(int processID) throws DebuggerException {
    throw new DebuggerException("Should not be called on RemoteDebuggerClient");
  }

  /** Unimplemented in this class (remote remoteDebugger should already be attached) */
  public void attach(String executableName, String coreFileName) throws DebuggerException {
    throw new DebuggerException("Should not be called on RemoteDebuggerClient");
  }

  /** Unimplemented in this class (remote remoteDebugger can not be detached) */
  public boolean detach() {
    throw new DebuggerException("Should not be called on RemoteDebuggerClient");
  }

  public Address parseAddress(String addressString) throws NumberFormatException {
    long addr = utils.scanAddress(addressString);
    if (addr == 0) {
      return null;
    }
    return new RemoteAddress(this, addr);
  }

  public String getOS() throws DebuggerException {
    try {
      return remoteDebugger.getOS();
    }
    catch (RemoteException e) {
      throw new DebuggerException(e);
    }
  }

  public String getCPU() {
    try {
      return remoteDebugger.getCPU();
    }
    catch (RemoteException e) {
      throw new DebuggerException(e);
    }
  }

  public boolean hasConsole() throws DebuggerException {
    try {
       return remoteDebugger.hasConsole();
    } catch (RemoteException e) {
       throw new DebuggerException(e);
    }
  }

  public String consoleExecuteCommand(String cmd) throws DebuggerException {
    try {
      return remoteDebugger.consoleExecuteCommand(cmd);
    }
    catch (RemoteException e) {
      throw new DebuggerException(e);
    }
  }

  public String getConsolePrompt() throws DebuggerException {
    try {
      return remoteDebugger.getConsolePrompt();
    } catch (RemoteException e) {
      throw new DebuggerException(e);
    }
  }

  public CDebugger getCDebugger() throws DebuggerException {
    return null;
  }

  //--------------------------------------------------------------------------------
  // Implementation of SymbolLookup interface

  public Address lookup(String objectName, String symbol) {
    try {
      long addr = remoteDebugger.lookupInProcess(objectName, symbol);
      if (addr == 0) {
        return null;
      }
      return new RemoteAddress(this, addr);
    }
    catch (RemoteException e) {
      throw new DebuggerException(e);
    }
  }

  public OopHandle lookupOop(String objectName, String symbol) {
    try {
      long addr = remoteDebugger.lookupInProcess(objectName, symbol);
      if (addr == 0) {
        return null;
      }
      return new RemoteOopHandle(this, addr);
    }
    catch (RemoteException e) {
      throw new DebuggerException(e);
    }
  }


  //--------------------------------------------------------------------------------
  // Implementation of JVMDebugger interface
  //

  /** Unimplemented in this class (remote remoteDebugger should already be configured) */
  public void configureJavaPrimitiveTypeSizes(long jbooleanSize,
                                              long jbyteSize,
                                              long jcharSize,
                                              long jdoubleSize,
                                              long jfloatSize,
                                              long jintSize,
                                              long jlongSize,
                                              long jshortSize) {
    throw new DebuggerException("Should not be called on RemoteDebuggerClient");
  }

  public void setMachineDescription(MachineDescription machDesc) {
    throw new DebuggerException("Should not be called on RemoteDebuggerClient");
  }

  public int getRemoteProcessAddressSize() {
    throw new DebuggerException("Should not be called on RemoteDebuggerClient");
  }

  public String addressValueToString(long addr) {
    return utils.addressValueToString(addr);
  }

  public long getAddressValue(Address addr) throws DebuggerException {
    if (addr == null) return 0;
    return addr.asLongValue();
  }

  public Address newAddress(long value) {
    if (value == 0) return null;
    return new RemoteAddress(this, value);
  }

  RemoteAddress readAddress(long address)
    throws UnmappedAddressException, UnalignedAddressException {
    long value = readAddressValue(address);
    return (value == 0 ? null : new RemoteAddress(this, value));
  }

  RemoteAddress readCompOopAddress(long address)
    throws UnmappedAddressException, UnalignedAddressException {
    long value = readCompOopAddressValue(address);
    return (value == 0 ? null : new RemoteAddress(this, value));
  }

  RemoteAddress readCompKlassAddress(long address)
    throws UnmappedAddressException, UnalignedAddressException {
    long value = readCompKlassAddressValue(address);
    return (value == 0 ? null : new RemoteAddress(this, value));
  }

  RemoteOopHandle readOopHandle(long address)
    throws UnmappedAddressException, UnalignedAddressException, NotInHeapException {
    long value = readAddressValue(address);
    return (value == 0 ? null : new RemoteOopHandle(this, value));
  }

  RemoteOopHandle readCompOopHandle(long address)
    throws UnmappedAddressException, UnalignedAddressException, NotInHeapException {
    long value = readCompOopAddressValue(address);
    return (value == 0 ? null : new RemoteOopHandle(this, value));
  }

  boolean areThreadsEqual(Address addr1, Address addr2) {
    try {
       return remoteDebugger.areThreadsEqual(getAddressValue(addr1), true,
                                             getAddressValue(addr2), true);
    } catch (RemoteException e) {
    }
    return false;
  }

  boolean areThreadsEqual(long id1, long id2) {
    try {
       return remoteDebugger.areThreadsEqual(id1, false, id2, false);
    } catch (RemoteException e) {
    }
    return false;
  }

  boolean areThreadsEqual(Address addr1, long id2) {
    try {
       return remoteDebugger.areThreadsEqual(getAddressValue(addr1), true, id2, false);
    } catch (RemoteException e) {
    }
    return false;
  }

  boolean areThreadsEqual(long id1, Address addr2) {
    try {
       return remoteDebugger.areThreadsEqual(id1, false, getAddressValue(addr2), true);
    } catch (RemoteException e) {
    }
    return false;
  }

  int getThreadHashCode(Address a) {
    try {
       return remoteDebugger.getThreadHashCode(getAddressValue(a), true);
    } catch (RemoteException e) {
    }
    return a.hashCode();
  }

  int getThreadHashCode(long id) {
    try {
       return remoteDebugger.getThreadHashCode(id, false);
    } catch (RemoteException e) {
    }
    return Long.hashCode(id);
  }

  public ThreadProxy getThreadForIdentifierAddress(Address addr) {
     return threadFactory.createThreadWrapper(addr);
  }

  public ThreadProxy getThreadForThreadId(long id) {
     return threadFactory.createThreadWrapper(id);
  }

  public MachineDescription getMachineDescription() throws DebuggerException {
     return machDesc;
  }

  /** This reads bytes from the remote process. */
  public ReadResult readBytesFromProcess(long address, long numBytes) {
    try {
      return remoteDebugger.readBytesFromProcess(address, numBytes);
    }
    catch (RemoteException e) {
      throw new DebuggerException(e);
    }
  }

  public String execCommandOnServer(String command, Map<String, Object> options) {
    try {
      return remoteDebugger.execCommandOnServer(command, options);
    } catch (RemoteException e) {
      throw new DebuggerException(e);
    }
  }

  @Override
  public String findSymbol(String symbol) {
    return execCommandOnServer("findsym", Map.of("symbol", symbol));
  }
}
