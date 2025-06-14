/*
SealedShouldHavePermitsList

*/
// Java17
package com.puppycrawl.tools.checkstyle.checks.design.sealedshouldhavepermitslist;

public class InputSealedShouldHavePermitsListInnerClass {
   // violation below 'Sealed classes or interfaces should explicitly declare permitted subclasses'
    sealed class A {}
    final class B extends A {}
    final class C extends A {}
    final class D { } // this can extend A, so as any other class in the compilation unit
    non-sealed class F extends A {}
    sealed class G extends A {}
    // violation above 'Sealed classes or interfaces should explicitly declare permitted subclasses'
    final class I extends G {}
    enum E {}
    record R(int x) {}
}

class InputSealedShouldHavePermitsListInnerClassCorrected {
    sealed class A permits B, C, F, G { } // ok, explicitly declared the permitted subclasses
    final class B extends A { }
    final class C extends A { }
    final class D { }    // this can't extend A
    non-sealed class F extends A {}
    sealed class G extends A permits I {}
    final class I extends G {}
    enum E {}
    record R(int x) {}
}
