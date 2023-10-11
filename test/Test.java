package test;

class HelloHelper4 {
  public static void printhi(String toadd) {
    System.out.println("Hello " + toadd);
  }
}

class HelloHelper3 {
  public static void printhi(String toadd) {
    HelloHelper4.printhi(toadd);
  }
}

class HelloHelper2 {
  public static void printhi(String toadd) {
    HelloHelper3.printhi(toadd);
  }
}

class HelloHelper {
  public static void printhi(String toadd) {
    HelloHelper2.printhi(toadd);
  }
}

public class Test {

  public static void main(String[] args) {
    HelloHelper.printhi("World!");
  }
}
