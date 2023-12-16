/*
 * Copyright (c) 2023 Airbyte, Inc., all rights reserved.
 */

package io.airbyte.workers.config;

import static io.airbyte.workers.config.ContainerOrchestratorConfigBeanFactory.STATE_STORAGE_PREFIX;

import io.airbyte.config.storage.CloudStorageConfigs;
import io.airbyte.config.storage.CloudStorageConfigs.GcsConfig;
import io.airbyte.config.storage.CloudStorageConfigs.MinioConfig;
import io.airbyte.config.storage.CloudStorageConfigs.S3Config;
import io.airbyte.workers.storage.DockerComposeDocumentStoreClient;
import io.airbyte.workers.storage.DocumentStoreClient;
import io.airbyte.workers.storage.StateClients;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Value;
import io.micronaut.context.env.Environment;
import io.micronaut.core.util.StringUtils;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Micronaut bean factory for cloud storage-related singletons.
 */
@Factory
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class CloudStorageBeanFactory {

  @SuppressWarnings("LineLength")

  @Singleton
  @Requires(property = "airbyte.cloud.storage.logs.type",
            pattern = "(?i)^gcs$")
  @Named("logStorageConfigs")
  public CloudStorageConfigs gcsLogStorageConfigs(
                                                  @Value("${airbyte.cloud.storage.logs.gcs.bucket}") final String gcsLogBucket,
                                                  @Value("${airbyte.cloud.storage.logs.gcs.application-credentials}") final String googleApplicationCredentials) {
    return CloudStorageConfigs.gcs(new GcsConfig(gcsLogBucket, googleApplicationCredentials));
  }

  @Singleton
  @Requires(property = "airbyte.cloud.storage.logs.type",
            pattern = "(?i)^minio$")
  @Named("logStorageConfigs")
  public CloudStorageConfigs minioLogStorageConfigs(
                                                    @Value("${airbyte.cloud.storage.logs.minio.access-key}") final String awsAccessKeyId,
                                                    @Value("${airbyte.cloud.storage.logs.minio.secret-access-key}") final String awsSecretAccessKey,
                                                    @Value("${airbyte.cloud.storage.logs.minio.bucket}") final String s3LogBucket,
                                                    @Value("${airbyte.cloud.storage.logs.minio.endpoint}") final String s3MinioEndpoint) {
    return CloudStorageConfigs.minio(new MinioConfig(s3LogBucket, awsAccessKeyId, awsSecretAccessKey, s3MinioEndpoint));
  }

  @Singleton
  @Requires(property = "airbyte.cloud.storage.logs.type",
            pattern = "(?i)^s3$")
  @Named("logStorageConfigs")
  public CloudStorageConfigs s3LogStorageConfigs(
                                                 @Value("${airbyte.cloud.storage.logs.s3.access-key}") final String awsAccessKeyId,
                                                 @Value("${airbyte.cloud.storage.logs.s3.secret-access-key}") final String awsSecretAccessKey,
                                                 @Value("${airbyte.cloud.storage.logs.s3.bucket}") final String s3LogBucket,
                                                 @Value("${airbyte.cloud.storage.logs.s3.region}") final String s3LogBucketRegion) {
    return CloudStorageConfigs.s3(new S3Config(s3LogBucket, awsAccessKeyId, awsSecretAccessKey, s3LogBucketRegion));
  }

  @SuppressWarnings("LineLength")

  @Singleton
  @Requires(property = "airbyte.cloud.storage.state.type",
            pattern = "(?i)^gcs$")
  @Named("stateStorageConfigs")
  public CloudStorageConfigs gcsStateStorageConfiguration(
                                                          @Value("${airbyte.cloud.storage.state.gcs.bucket}") final String gcsBucketName,
                                                          @Value("${airbyte.cloud.storage.state.gcs.application-credentials}") final String gcsApplicationCredentials) {
    return CloudStorageConfigs.gcs(new GcsConfig(gcsBucketName, gcsApplicationCredentials));
  }

  @SuppressWarnings("LineLength")

  @Singleton
  @Requires(property = "airbyte.cloud.storage.state.type",
            pattern = "(?i)^minio$")
  @Named("stateStorageConfigs")
  public CloudStorageConfigs minioStateStorageConfiguration(
                                                            @Value("${airbyte.cloud.storage.state.minio.bucket}") final String bucketName,
                                                            @Value("${airbyte.cloud.storage.state.minio.access-key}") final String awsAccessKey,
                                                            @Value("${airbyte.cloud.storage.state.minio.secret-access-key}") final String secretAccessKey,
                                                            @Value("${airbyte.cloud.storage.state.minio.endpoint}") final String endpoint) {
    return CloudStorageConfigs.minio(new MinioConfig(bucketName, awsAccessKey, secretAccessKey, endpoint));
  }

  @Singleton
  @Requires(property = "airbyte.cloud.storage.state.type",
            pattern = "(?i)^s3$")
  @Named("stateStorageConfigs")
  public CloudStorageConfigs s3StateStorageConfiguration(
                                                         @Value("${airbyte.cloud.storage.state.s3.bucket}") final String bucketName,
                                                         @Value("${airbyte.cloud.storage.state.s3.access-key}") final String awsAccessKey,
                                                         @Value("${airbyte.cloud.storage.state.s3.secret-access-key}") final String secretAcessKey,
                                                         @Value("${airbyte.cloud.storage.state.s3.region}") final String s3Region) {
    return CloudStorageConfigs.s3(new S3Config(bucketName, awsAccessKey, secretAcessKey, s3Region));
  }

  @Singleton
  @Requires(env = Environment.KUBERNETES)
  public DocumentStoreClient documentStoreClient(@Named("stateStorageConfigs") final Optional<CloudStorageConfigs> cloudStateStorageConfiguration) {
    return StateClients.create(cloudStateStorageConfiguration.orElse(null), STATE_STORAGE_PREFIX);
  }

  @Singleton
  @Requires(notEnv = Environment.KUBERNETES)
  public DocumentStoreClient documentStoreClient(@Value("${airbyte.workspace.root}") final String workspaceRoot,
                                                 @Value("${airbyte.workspace.docker-mount}") final String workspaceDockerMount) {
    return new DockerComposeDocumentStoreClient(
        StringUtils.isNotEmpty(workspaceDockerMount) ? Path.of(workspaceDockerMount) : Path.of(workspaceRoot));
  }

}
