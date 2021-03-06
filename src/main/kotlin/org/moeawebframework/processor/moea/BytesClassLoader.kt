package org.moeawebframework.processor.moea

internal class BytesClassLoader<T>(parentClassLoader: ClassLoader) : ClassLoader(parentClassLoader) {

  @Suppress("UNCHECKED_CAST")
  fun loadClassFromBytes(classData: ByteArray): Class<T> {
    return defineClass(null, classData, 0, classData.size) as Class<T>
  }

}