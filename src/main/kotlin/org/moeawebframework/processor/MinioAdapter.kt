package org.moeawebframework.processor

import io.minio.GetObjectArgs
import io.minio.MinioClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class MinioAdapter(
    private val minioClient: MinioClient
) {

  @Value("\${minio.bucket}")
  lateinit var bucket: String

  fun download(key: String): ByteArray? {
    val objectArgs = GetObjectArgs.builder()
        .bucket(bucket)
        .`object`(key)
        .build()
    return try {
      val inputStream = minioClient.getObject(objectArgs)
      val bytes = inputStream.readAllBytes()
      inputStream.close()
      bytes
    } catch (e: Exception) {
      null
    }
  }

}