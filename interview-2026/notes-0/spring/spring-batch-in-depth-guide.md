# Spring Batch In-Depth Interview Guide: Batch Processing & Job Scheduling

## Table of Contents
1. [Spring Batch Overview](#spring-batch-overview)
2. [Batch Processing Concepts](#batch-processing-concepts)
3. [Job Configuration](#job-configuration)
4. [Item Processing](#item-processing)
5. [Chunk Processing](#chunk-processing)
6. [Job Scheduling](#job-scheduling)
7. [Error Handling](#error-handling)
8. [Parallel Processing](#parallel-processing)
9. [Best Practices](#best-practices)
10. [Interview Questions & Answers](#interview-questions--answers)

---

## Spring Batch Overview

### What is Spring Batch?

**Spring Batch** is a framework for:
- **Batch Processing**: Processing large volumes of data
- **Job Scheduling**: Running jobs at specific times
- **Chunk Processing**: Processing data in chunks
- **Transaction Management**: Managing transactions per chunk
- **Restartability**: Resume failed jobs from last checkpoint

### Key Benefits

1. **Large Volume Processing**: Handle millions of records
2. **Chunk Processing**: Process in configurable chunks
3. **Transaction Management**: Commit per chunk
4. **Error Handling**: Skip, retry, and recovery mechanisms
5. **Job Monitoring**: Track job execution and status
6. **Restartability**: Resume from last successful chunk

### Spring Batch Architecture

```
Job
  ↓
JobInstance (Logical job execution)
  ↓
JobExecution (Physical job execution)
  ↓
StepExecution (Step execution)
  ↓
Chunk (ItemReader → ItemProcessor → ItemWriter)
```

### Dependencies

```xml
<dependency>
    <groupId>org.springframework.batch</groupId>
    <artifactId>spring-boot-starter-batch</artifactId>
</dependency>
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>
```

**Note**: Spring Batch requires a database to store job metadata.

---

## Batch Processing Concepts

### Core Components

#### 1. **Job**

**Job** is the batch process that:
- Contains one or more Steps
- Has a unique name
- Can be parameterized
- Can be restarted

```java
@Configuration
@EnableBatchProcessing
public class BatchConfig {
    
    @Bean
    public Job userProcessingJob(JobRepository jobRepository, Step step1) {
        return new JobBuilder("userProcessingJob", jobRepository)
                .start(step1)
                .build();
    }
}
```

#### 2. **Step**

**Step** is a phase of a Job:
- Contains ItemReader, ItemProcessor, ItemWriter
- Can be chunk-based or tasklet-based
- Has its own transaction boundary

```java
@Bean
public Step processUserStep(
        JobRepository jobRepository,
        PlatformTransactionManager transactionManager,
        ItemReader<User> reader,
        ItemProcessor<User, UserDTO> processor,
        ItemWriter<UserDTO> writer) {
    
    return new StepBuilder("processUserStep", jobRepository)
            .<User, UserDTO>chunk(10, transactionManager)
            .reader(reader)
            .processor(processor)
            .writer(writer)
            .build();
}
```

#### 3. **ItemReader**

**ItemReader** reads data from source:
- Reads one item at a time
- Returns null when no more items
- Can be file-based, database-based, or custom

```java
@Bean
public ItemReader<User> userItemReader() {
    return new JdbcCursorItemReaderBuilder<User>()
            .name("userItemReader")
            .dataSource(dataSource)
            .sql("SELECT id, name, email FROM users WHERE processed = false")
            .rowMapper(new BeanPropertyRowMapper<>(User.class))
            .build();
}
```

#### 4. **ItemProcessor**

**ItemProcessor** processes/transforms items:
- Optional component
- Transforms input to output
- Can filter items (return null)
- Can validate items

```java
@Bean
public ItemProcessor<User, UserDTO> userItemProcessor() {
    return new ItemProcessor<User, UserDTO>() {
        @Override
        public UserDTO process(User user) throws Exception {
            // Transform User to UserDTO
            return new UserDTO(user.getId(), user.getName(), user.getEmail());
        }
    };
}
```

#### 5. **ItemWriter**

**ItemWriter** writes processed items:
- Writes chunk of items
- Receives List of items
- Can write to file, database, or custom destination

```java
@Bean
public ItemWriter<UserDTO> userItemWriter() {
    return new JdbcBatchItemWriterBuilder<UserDTO>()
            .dataSource(dataSource)
            .sql("INSERT INTO processed_users (id, name, email) VALUES (?, ?, ?)")
            .itemPreparedStatementSetter(new UserDTOPreparedStatementSetter())
            .build();
}
```

### Job Repository

**JobRepository** stores job metadata:
- Job instances
- Job executions
- Step executions
- Job parameters

```java
@Configuration
@EnableBatchProcessing
public class BatchConfig {
    
    @Bean
    public JobRepository jobRepository(DataSource dataSource, PlatformTransactionManager transactionManager) {
        return new JobRepositoryFactoryBean()
                .setDataSource(dataSource)
                .setTransactionManager(transactionManager)
                .setDatabaseType("H2")
                .setIsolationLevelForCreate("ISOLATION_SERIALIZABLE")
                .setTablePrefix("BATCH_")
                .setMaxVarCharLength(1000)
                .getObject();
    }
    
    @Bean
    public JobLauncher jobLauncher(JobRepository jobRepository) {
        SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
        jobLauncher.setJobRepository(jobRepository);
        jobLauncher.setTaskExecutor(new SimpleAsyncTaskExecutor());
        return jobLauncher;
    }
}
```

**Spring Boot Auto-Configuration:**

Spring Boot automatically configures JobRepository and JobLauncher when using `@EnableBatchProcessing`.

---

## Job Configuration

### Basic Job

```java
@Configuration
@EnableBatchProcessing
public class BatchConfig {
    
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    
    public BatchConfig(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
    }
    
    @Bean
    public Job userProcessingJob(Step processUserStep) {
        return new JobBuilder("userProcessingJob", jobRepository)
                .start(processUserStep)
                .build();
    }
    
    @Bean
    public Step processUserStep(
            ItemReader<User> reader,
            ItemProcessor<User, UserDTO> processor,
            ItemWriter<UserDTO> writer) {
        
        return new StepBuilder("processUserStep", jobRepository)
                .<User, UserDTO>chunk(10, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }
}
```

### Job with Multiple Steps

```java
@Bean
public Job multiStepJob(
        Step step1,
        Step step2,
        Step step3) {
    return new JobBuilder("multiStepJob", jobRepository)
            .start(step1)
            .next(step2)
            .next(step3)
            .build();
}
```

### Conditional Flow

```java
@Bean
public Job conditionalJob(
        Step step1,
        Step step2,
        Step step3) {
    return new JobBuilder("conditionalJob", jobRepository)
            .start(step1)
            .on("COMPLETED").to(step2)
            .on("FAILED").to(step3)
            .end()
            .build();
}
```

### Job Parameters

**Passing Parameters:**

```java
@Service
public class JobService {
    
    private final JobLauncher jobLauncher;
    private final Job userProcessingJob;
    
    public JobService(JobLauncher jobLauncher, Job userProcessingJob) {
        this.jobLauncher = jobLauncher;
        this.userProcessingJob = userProcessingJob;
    }
    
    public void runJob(String inputFile, String outputFile) {
        JobParameters parameters = new JobParametersBuilder()
                .addString("inputFile", inputFile)
                .addString("outputFile", outputFile)
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();
        
        try {
            jobLauncher.run(userProcessingJob, parameters);
        } catch (Exception e) {
            throw new RuntimeException("Job execution failed", e);
        }
    }
}
```

**Using Parameters in Steps:**

```java
@Bean
@StepScope
public ItemReader<User> userItemReader(
        @Value("#{jobParameters['inputFile']}") String inputFile) {
    return new FlatFileItemReaderBuilder<User>()
            .name("userItemReader")
            .resource(new FileSystemResource(inputFile))
            .delimited()
            .names("id", "name", "email")
            .targetType(User.class)
            .build();
}
```

### Job Listeners

```java
@Component
public class JobExecutionListener implements org.springframework.batch.core.JobExecutionListener {
    
    @Override
    public void beforeJob(JobExecution jobExecution) {
        System.out.println("Job starting: " + jobExecution.getJobInstance().getJobName());
    }
    
    @Override
    public void afterJob(JobExecution jobExecution) {
        BatchStatus status = jobExecution.getStatus();
        System.out.println("Job completed with status: " + status);
        
        if (status == BatchStatus.COMPLETED) {
            System.out.println("Job completed successfully");
        } else if (status == BatchStatus.FAILED) {
            System.out.println("Job failed");
            jobExecution.getFailureExceptions().forEach(ex -> 
                System.out.println("Error: " + ex.getMessage())
            );
        }
    }
}

// Register listener
@Bean
public Job userProcessingJob(Step processUserStep, JobExecutionListener listener) {
    return new JobBuilder("userProcessingJob", jobRepository)
            .start(processUserStep)
            .listener(listener)
            .build();
}
```

---

## Item Processing

### File-Based ItemReader

**FlatFileItemReader (CSV):**

```java
@Bean
public ItemReader<User> csvFileReader(
        @Value("${input.file}") String inputFile) {
    return new FlatFileItemReaderBuilder<User>()
            .name("csvFileReader")
            .resource(new FileSystemResource(inputFile))
            .delimited()
            .names("id", "name", "email", "age")
            .fieldSetMapper(new BeanWrapperFieldSetMapper<User>() {{
                setTargetType(User.class);
            }})
            .linesToSkip(1)  // Skip header
            .build();
}
```

**Fixed Width File:**

```java
@Bean
public ItemReader<User> fixedWidthFileReader(
        @Value("${input.file}") String inputFile) {
    return new FlatFileItemReaderBuilder<User>()
            .name("fixedWidthFileReader")
            .resource(new FileSystemResource(inputFile))
            .fixedLength()
            .columns(new Range(1, 10), new Range(11, 30), new Range(31, 60))
            .names("id", "name", "email")
            .fieldSetMapper(new BeanWrapperFieldSetMapper<User>() {{
                setTargetType(User.class);
            }})
            .build();
}
```

### Database-Based ItemReader

**JdbcCursorItemReader:**

```java
@Bean
public ItemReader<User> jdbcCursorReader(DataSource dataSource) {
    return new JdbcCursorItemReaderBuilder<User>()
            .name("jdbcCursorReader")
            .dataSource(dataSource)
            .sql("SELECT id, name, email, age FROM users WHERE processed = false")
            .rowMapper(new BeanPropertyRowMapper<>(User.class))
            .fetchSize(100)
            .build();
}
```

**JdbcPagingItemReader:**

```java
@Bean
public ItemReader<User> jdbcPagingReader(DataSource dataSource) {
    Map<String, Object> parameterValues = new HashMap<>();
    parameterValues.put("status", "PENDING");
    
    return new JdbcPagingItemReaderBuilder<User>()
            .name("jdbcPagingReader")
            .dataSource(dataSource)
            .queryProvider(createQueryProvider(dataSource))
            .parameterValues(parameterValues)
            .pageSize(100)
            .rowMapper(new BeanPropertyRowMapper<>(User.class))
            .build();
}

private PagingQueryProvider createQueryProvider(DataSource dataSource) {
    SqlPagingQueryProviderFactoryBean queryProvider = new SqlPagingQueryProviderFactoryBean();
    queryProvider.setDataSource(dataSource);
    queryProvider.setSelectClause("SELECT id, name, email, age");
    queryProvider.setFromClause("FROM users");
    queryProvider.setWhereClause("WHERE status = :status");
    queryProvider.setSortKey("id");
    return queryProvider.getObject();
}
```

**JpaPagingItemReader:**

```java
@Bean
public ItemReader<User> jpaPagingReader(EntityManagerFactory entityManagerFactory) {
    return new JpaPagingItemReaderBuilder<User>()
            .name("jpaPagingReader")
            .entityManagerFactory(entityManagerFactory)
            .queryString("SELECT u FROM User u WHERE u.processed = false")
            .pageSize(100)
            .build();
}
```

### Custom ItemReader

```java
@Component
public class CustomItemReader implements ItemReader<User> {
    
    private final UserRepository userRepository;
    private Iterator<User> userIterator;
    
    public CustomItemReader(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @Override
    public User read() throws Exception {
        if (userIterator == null) {
            List<User> users = userRepository.findUnprocessedUsers();
            userIterator = users.iterator();
        }
        
        return userIterator.hasNext() ? userIterator.next() : null;
    }
}
```

### ItemProcessor

**Transformation:**

```java
@Component
public class UserItemProcessor implements ItemProcessor<User, UserDTO> {
    
    @Override
    public UserDTO process(User user) throws Exception {
        // Transform User to UserDTO
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setName(user.getName().toUpperCase());
        dto.setEmail(user.getEmail().toLowerCase());
        dto.setAge(user.getAge());
        return dto;
    }
}
```

**Filtering (Return null to skip):**

```java
@Component
public class UserItemProcessor implements ItemProcessor<User, UserDTO> {
    
    @Override
    public UserDTO process(User user) throws Exception {
        // Skip users under 18
        if (user.getAge() < 18) {
            return null;  // Item will be skipped
        }
        
        return new UserDTO(user.getId(), user.getName(), user.getEmail());
    }
}
```

**Validation:**

```java
@Component
public class UserItemProcessor implements ItemProcessor<User, UserDTO> {
    
    private final Validator validator;
    
    public UserItemProcessor(Validator validator) {
        this.validator = validator;
    }
    
    @Override
    public UserDTO process(User user) throws Exception {
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        
        if (!violations.isEmpty()) {
            throw new ValidationException("Validation failed: " + violations);
        }
        
        return new UserDTO(user.getId(), user.getName(), user.getEmail());
    }
}
```

**Composite Processor:**

```java
@Bean
public ItemProcessor<User, UserDTO> compositeProcessor() {
    CompositeItemProcessor<User, UserDTO> processor = new CompositeItemProcessor<>();
    processor.setDelegates(Arrays.asList(
            new UserValidationProcessor(),
            new UserTransformationProcessor(),
            new UserEnrichmentProcessor()
    ));
    return processor;
}
```

### ItemWriter

**File-Based Writer:**

```java
@Bean
public ItemWriter<UserDTO> csvFileWriter(
        @Value("${output.file}") String outputFile) {
    return new FlatFileItemWriterBuilder<UserDTO>()
            .name("csvFileWriter")
            .resource(new FileSystemResource(outputFile))
            .delimited()
            .delimiter(",")
            .names("id", "name", "email")
            .headerCallback(writer -> writer.write("ID,Name,Email"))
            .build();
}
```

**Database Writer:**

```java
@Bean
public ItemWriter<UserDTO> jdbcBatchWriter(DataSource dataSource) {
    return new JdbcBatchItemWriterBuilder<UserDTO>()
            .dataSource(dataSource)
            .sql("INSERT INTO processed_users (id, name, email) VALUES (?, ?, ?)")
            .itemPreparedStatementSetter(new ItemPreparedStatementSetter<UserDTO>() {
                @Override
                public void setValues(UserDTO item, PreparedStatement ps) throws SQLException {
                    ps.setLong(1, item.getId());
                    ps.setString(2, item.getName());
                    ps.setString(3, item.getEmail());
                }
            })
            .build();
}
```

**JPA Writer:**

```java
@Bean
public ItemWriter<UserDTO> jpaWriter(EntityManagerFactory entityManagerFactory) {
    JpaItemWriter<UserDTO> writer = new JpaItemWriter<>();
    writer.setEntityManagerFactory(entityManagerFactory);
    return writer;
}
```

**Custom Writer:**

```java
@Component
public class CustomItemWriter implements ItemWriter<UserDTO> {
    
    private final UserRepository userRepository;
    
    public CustomItemWriter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @Override
    public void write(List<? extends UserDTO> items) throws Exception {
        for (UserDTO item : items) {
            // Custom write logic
            userRepository.save(convertToEntity(item));
        }
    }
    
    private User convertToEntity(UserDTO dto) {
        // Conversion logic
        return new User(dto.getId(), dto.getName(), dto.getEmail());
    }
}
```

---

## Chunk Processing

### Chunk Configuration

**Chunk Size:**

```java
@Bean
public Step processUserStep(
        ItemReader<User> reader,
        ItemProcessor<User, UserDTO> processor,
        ItemWriter<UserDTO> writer) {
    
    return new StepBuilder("processUserStep", jobRepository)
            .<User, UserDTO>chunk(100, transactionManager)  // Process 100 items per chunk
            .reader(reader)
            .processor(processor)
            .writer(writer)
            .build();
}
```

**Chunk Processing Flow:**

```
1. Read items (up to chunk size)
   ↓
2. Process each item
   ↓
3. Collect processed items
   ↓
4. Write chunk (transaction boundary)
   ↓
5. Commit transaction
   ↓
6. Repeat until no more items
```

### Chunk-Oriented Processing

**Benefits:**
- **Transaction Management**: One transaction per chunk
- **Memory Efficiency**: Process in manageable chunks
- **Error Recovery**: Can restart from last successful chunk
- **Performance**: Batch writes are more efficient

**Example:**

```java
@Bean
public Step processUserStep(
        ItemReader<User> reader,
        ItemProcessor<User, UserDTO> processor,
        ItemWriter<UserDTO> writer) {
    
    return new StepBuilder("processUserStep", jobRepository)
            .<User, UserDTO>chunk(50, transactionManager)
            .reader(reader)
            .processor(processor)
            .writer(writer)
            .faultTolerant()
            .skipLimit(10)
            .skip(Exception.class)
            .retryLimit(3)
            .retry(SQLException.class)
            .build();
}
```

---

## Job Scheduling

### Spring Scheduler

**Enable Scheduling:**

```java
@SpringBootApplication
@EnableScheduling
public class BatchApplication {
    public static void main(String[] args) {
        SpringApplication.run(BatchApplication.class, args);
    }
}
```

**Scheduled Job:**

```java
@Component
public class ScheduledJobRunner {
    
    private final JobLauncher jobLauncher;
    private final Job userProcessingJob;
    
    public ScheduledJobRunner(JobLauncher jobLauncher, Job userProcessingJob) {
        this.jobLauncher = jobLauncher;
        this.userProcessingJob = userProcessingJob;
    }
    
    @Scheduled(cron = "0 0 2 * * ?")  // Run at 2 AM daily
    public void runJob() {
        JobParameters parameters = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();
        
        try {
            jobLauncher.run(userProcessingJob, parameters);
        } catch (Exception e) {
            throw new RuntimeException("Scheduled job failed", e);
        }
    }
}
```

### Cron Expressions

**Cron Format:** `second minute hour day month weekday`

```java
@Scheduled(cron = "0 0 2 * * ?")        // 2 AM daily
@Scheduled(cron = "0 0 */6 * * ?")      // Every 6 hours
@Scheduled(cron = "0 0 9 * * MON-FRI")  // 9 AM weekdays
@Scheduled(cron = "0 0 0 1 * ?")        // First day of month
@Scheduled(cron = "0 */15 * * * ?")     // Every 15 minutes
```

**Cron Special Characters:**
- `*`: Any value
- `?`: No specific value (day/month or day/week)
- `-`: Range (e.g., MON-FRI)
- `,`: List (e.g., MON,WED,FRI)
- `/`: Increment (e.g., */5 = every 5)

### Fixed Rate/Delay

```java
@Scheduled(fixedRate = 5000)        // Every 5 seconds
@Scheduled(fixedDelay = 10000)      // 10 seconds after previous completion
@Scheduled(initialDelay = 60000, fixedRate = 300000)  // Start after 1 min, then every 5 min
```

### Quartz Scheduler

**Dependencies:**

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-quartz</artifactId>
</dependency>
```

**Quartz Job:**

```java
public class UserProcessingJob extends QuartzJobBean {
    
    @Autowired
    private JobLauncher jobLauncher;
    
    @Autowired
    private Job userProcessingJob;
    
    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        JobParameters parameters = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();
        
        try {
            jobLauncher.run(userProcessingJob, parameters);
        } catch (Exception e) {
            throw new JobExecutionException("Job execution failed", e);
        }
    }
}
```

**Quartz Configuration:**

```java
@Configuration
public class QuartzConfig {
    
    @Bean
    public JobDetail userProcessingJobDetail() {
        return JobBuilder.newJob(UserProcessingJob.class)
                .withIdentity("userProcessingJob")
                .storeDurably()
                .build();
    }
    
    @Bean
    public Trigger userProcessingJobTrigger() {
        return TriggerBuilder.newTrigger()
                .forJob(userProcessingJobDetail())
                .withIdentity("userProcessingJobTrigger")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 2 * * ?"))
                .build();
    }
}
```

---

## Error Handling

### Skip Policy

**Skip Items on Error:**

```java
@Bean
public Step processUserStep(
        ItemReader<User> reader,
        ItemProcessor<User, UserDTO> processor,
        ItemWriter<UserDTO> writer) {
    
    return new StepBuilder("processUserStep", jobRepository)
            .<User, UserDTO>chunk(10, transactionManager)
            .reader(reader)
            .processor(processor)
            .writer(writer)
            .faultTolerant()
            .skipLimit(100)  // Skip up to 100 items
            .skip(DataIntegrityViolationException.class)
            .skip(ValidationException.class)
            .noSkip(IllegalArgumentException.class)  // Don't skip these
            .build();
}
```

**Custom Skip Policy:**

```java
@Component
public class CustomSkipPolicy implements SkipPolicy {
    
    @Override
    public boolean shouldSkip(Throwable t, int skipCount) {
        if (t instanceof ValidationException && skipCount < 50) {
            return true;
        }
        return false;
    }
}

// Use in step
.faultTolerant()
.skipPolicy(customSkipPolicy)
```

### Retry Policy

**Retry on Transient Errors:**

```java
@Bean
public Step processUserStep(
        ItemReader<User> reader,
        ItemProcessor<User, UserDTO> processor,
        ItemWriter<UserDTO> writer) {
    
    return new StepBuilder("processUserStep", jobRepository)
            .<User, UserDTO>chunk(10, transactionManager)
            .reader(reader)
            .processor(processor)
            .writer(writer)
            .faultTolerant()
            .retryLimit(3)
            .retry(DeadlockLoserDataAccessException.class)
            .retry(SQLException.class)
            .noRetry(IllegalArgumentException.class)
            .build();
}
```

**Retry Template:**

```java
@Bean
public RetryTemplate retryTemplate() {
    RetryTemplate retryTemplate = new RetryTemplate();
    
    FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
    backOffPolicy.setBackOffPeriod(1000);  // 1 second
    retryTemplate.setBackOffPolicy(backOffPolicy);
    
    SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
    retryPolicy.setMaxAttempts(3);
    retryTemplate.setRetryPolicy(retryPolicy);
    
    return retryTemplate;
}
```

### Error Listeners

**Step Execution Listener:**

```java
@Component
public class StepExecutionListener implements org.springframework.batch.core.StepExecutionListener {
    
    @Override
    public void beforeStep(StepExecution stepExecution) {
        System.out.println("Step starting: " + stepExecution.getStepName());
    }
    
    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        System.out.println("Step completed: " + stepExecution.getStepName());
        System.out.println("Items read: " + stepExecution.getReadCount());
        System.out.println("Items written: " + stepExecution.getWriteCount());
        System.out.println("Items skipped: " + stepExecution.getSkipCount());
        
        if (stepExecution.getFailureExceptions().isEmpty()) {
            return ExitStatus.COMPLETED;
        } else {
            return ExitStatus.FAILED;
        }
    }
}

// Register listener
@Bean
public Step processUserStep(
        ItemReader<User> reader,
        ItemProcessor<User, UserDTO> processor,
        ItemWriter<UserDTO> writer,
        StepExecutionListener listener) {
    
    return new StepBuilder("processUserStep", jobRepository)
            .<User, UserDTO>chunk(10, transactionManager)
            .reader(reader)
            .processor(processor)
            .writer(writer)
            .listener(listener)
            .build();
}
```

**Chunk Listener:**

```java
@Component
public class ChunkListener implements ChunkListener {
    
    @Override
    public void beforeChunk(ChunkContext context) {
        System.out.println("Before chunk");
    }
    
    @Override
    public void afterChunk(ChunkContext context) {
        System.out.println("After chunk");
    }
    
    @Override
    public void afterChunkError(ChunkContext context) {
        System.out.println("Chunk error occurred");
    }
}
```

### Restartability

**Restart Failed Job:**

```java
@Service
public class JobService {
    
    private final JobLauncher jobLauncher;
    private final Job userProcessingJob;
    private final JobExplorer jobExplorer;
    
    public JobService(JobLauncher jobLauncher, Job userProcessingJob, JobExplorer jobExplorer) {
        this.jobLauncher = jobLauncher;
        this.userProcessingJob = userProcessingJob;
        this.jobExplorer = jobExplorer;
    }
    
    public void restartJob(Long jobInstanceId) {
        JobInstance jobInstance = jobExplorer.getJobInstance(jobInstanceId);
        List<JobExecution> jobExecutions = jobExplorer.getJobExecutions(jobInstance);
        
        JobExecution lastExecution = jobExecutions.get(0);
        if (lastExecution.getStatus() == BatchStatus.FAILED) {
            JobParameters parameters = lastExecution.getJobParameters();
            try {
                jobLauncher.run(userProcessingJob, parameters);
            } catch (Exception e) {
                throw new RuntimeException("Job restart failed", e);
            }
        }
    }
}
```

---

## Parallel Processing

### Multi-Threaded Step

```java
@Bean
public Step processUserStep(
        ItemReader<User> reader,
        ItemProcessor<User, UserDTO> processor,
        ItemWriter<UserDTO> writer) {
    
    return new StepBuilder("processUserStep", jobRepository)
            .<User, UserDTO>chunk(10, transactionManager)
            .reader(reader)
            .processor(processor)
            .writer(writer)
            .taskExecutor(new SimpleAsyncTaskExecutor())
            .throttleLimit(5)  // Max 5 threads
            .build();
}
```

### Parallel Steps

```java
@Bean
public Job parallelJob(
        Step step1,
        Step step2,
        Step step3) {
    return new JobBuilder("parallelJob", jobRepository)
            .start(step1)
            .split(new SimpleAsyncTaskExecutor())
            .add(step2, step3)
            .end()
            .build();
}
```

### Partitioning

**Partition Step:**

```java
@Bean
public Step partitionStep(
        Step slaveStep,
        Partitioner partitioner) {
    return new StepBuilder("partitionStep", jobRepository)
            .partitioner("slaveStep", partitioner)
            .step(slaveStep)
            .gridSize(4)  // 4 partitions
            .taskExecutor(new SimpleAsyncTaskExecutor())
            .build();
}

@Bean
public Partitioner partitioner(DataSource dataSource) {
    return new ColumnRangePartitioner() {{
        setDataSource(dataSource);
        setTable("users");
        setColumn("id");
    }};
}
```

---

## Best Practices

### Batch Processing Best Practices

1. **Chunk Size**: Optimize chunk size (100-1000 items)
2. **Transaction Management**: One transaction per chunk
3. **Error Handling**: Implement skip and retry policies
4. **Monitoring**: Track job execution metrics
5. **Restartability**: Design jobs to be restartable
6. **Idempotency**: Ensure idempotent operations
7. **Resource Management**: Close resources properly

### Job Scheduling Best Practices

1. **Cron Expressions**: Use clear, documented cron expressions
2. **Time Zones**: Specify time zones for cron jobs
3. **Job Parameters**: Use unique parameters (timestamp) to prevent duplicate runs
4. **Monitoring**: Monitor scheduled job execution
5. **Error Notification**: Notify on job failures
6. **Resource Usage**: Schedule during low-traffic periods

### Performance Best Practices

1. **Chunk Size**: Balance memory and performance
2. **Parallel Processing**: Use for CPU-intensive operations
3. **Database Optimization**: Use indexes, batch operations
4. **Connection Pooling**: Configure appropriate pool size
5. **Memory Management**: Process in chunks, avoid loading all data

---

## Interview Questions & Answers

### Q1: What is Spring Batch and when would you use it?

**Answer:**
- Framework for batch processing large volumes of data
- Use cases: ETL processes, data migration, report generation, bulk operations
- Benefits: Chunk processing, transaction management, restartability, error handling

### Q2: What is the difference between ItemReader, ItemProcessor, and ItemWriter?

**Answer:**
- **ItemReader**: Reads data from source (file, database), returns one item at a time
- **ItemProcessor**: Processes/transforms items, optional, can filter (return null)
- **ItemWriter**: Writes processed items, receives List of items, writes in batch

### Q3: What is chunk processing?

**Answer:**
- Process data in configurable chunks (e.g., 100 items)
- One transaction per chunk
- More efficient than item-by-item processing
- Enables restartability from last successful chunk

### Q4: How do you handle errors in Spring Batch?

**Answer:**
1. **Skip Policy**: Skip items that cause errors
2. **Retry Policy**: Retry transient errors
3. **Error Listeners**: Log and handle errors
4. **Restartability**: Restart failed jobs from last checkpoint

### Q5: What is JobRepository?

**Answer:**
- Stores job metadata (instances, executions, step executions)
- Required for Spring Batch
- Enables restartability and job monitoring
- Uses database to persist metadata

### Q6: How do you schedule Spring Batch jobs?

**Answer:**
1. **@Scheduled**: Spring's built-in scheduler with cron expressions
2. **Quartz**: More advanced scheduling with Quartz Scheduler
3. **JobLauncher**: Programmatically launch jobs
4. Use unique job parameters to prevent duplicate runs

### Q7: What is the difference between restartable and non-restartable jobs?

**Answer:**
- **Restartable**: Can be restarted from last checkpoint, uses JobRepository
- **Non-restartable**: Cannot be restarted, runs from beginning
- Restartable jobs are preferred for long-running batch processes

### Q8: How do you implement parallel processing in Spring Batch?

**Answer:**
1. **Multi-threaded Step**: Use TaskExecutor in step
2. **Parallel Steps**: Use split() to run steps in parallel
3. **Partitioning**: Partition data and process in parallel
4. Configure thread pool size appropriately

### Q9: What is @StepScope and why is it used?

**Answer:**
- Creates bean instance per step execution
- Allows late binding of job parameters
- Required when using job parameters in ItemReader/ItemWriter
- Enables thread-safe processing

### Q10: How do you monitor Spring Batch job execution?

**Answer:**
1. **JobExecutionListener**: Track job start/end
2. **StepExecutionListener**: Track step metrics
3. **JobExplorer**: Query job execution history
4. **Actuator**: Expose job metrics via Spring Boot Actuator
5. **Database**: Query BATCH_* tables for job metadata

---

## Summary

**Key Takeaways:**
1. **Spring Batch**: Framework for batch processing large data volumes
2. **Chunk Processing**: Process in configurable chunks with transaction boundaries
3. **Job Components**: Job → Step → ItemReader/ItemProcessor/ItemWriter
4. **Error Handling**: Skip, retry, and recovery mechanisms
5. **Job Scheduling**: @Scheduled or Quartz for scheduled execution
6. **Restartability**: Jobs can be restarted from last checkpoint
7. **Parallel Processing**: Multi-threaded steps, parallel steps, partitioning

**Complete Coverage:**
- Batch processing concepts and architecture
- Job and Step configuration
- ItemReader, ItemProcessor, ItemWriter implementations
- Chunk processing and transaction management
- Job scheduling with Spring Scheduler and Quartz
- Error handling (skip, retry, listeners)
- Parallel processing techniques
- Best practices and interview Q&A

---

**Guide Complete** - Ready for interview preparation!

