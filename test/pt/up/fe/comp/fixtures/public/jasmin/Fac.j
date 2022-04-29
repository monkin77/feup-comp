.class public Fac
.super java/lang/Object
.method public <init>()V
.limit stack 99
.limit locals 99
    aload 0
invokespecial Fac/<init>()V
.end method

.method public static main([Ljava/lang/String;)V
.limit stack 99
.limit locals 99
    new Fac
astore 1
    aload 1
invokespecial Fac/<init>()V
    aload 1
ldc 10
invokevirtual Fac/compFac(I)I
istore 3
    aload 4
iload 3
invokestatic io/println(I)V
.end method