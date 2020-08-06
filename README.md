# POC of Spring Cloud AWS testing with mock AWS endpoints

So, basically, when you create a Spring Cloud AWS application, and set up your unit
tests, you'll need to turn off Spring Cloud AWS, and create a custom `@TestConfiguration`
for your test AWS clients. That's because Spring Cloud AWS hooks into the Spring ApplicationContext
initialization cycle, (which hooks into the JUnit 5 execution cycle) and runs its own network 
requests before any of your application is initialized.

Or...

You can use the same code for testing and production, but hack into the JUnit 5 initialization
cycle and set up some mock AWS instance configuration endpoints as well as LocalStack, before
Spring Cloud AWS starts making network requests.

Then you hack into the AWS Java SDK initialization cycle, and point the AWS regions which you're
using, towards your LocalStack instance, after disabling TLS certificate verification for the
AWS Java SDK.

How?

You can hook into the JUnit 5 extension initialization cycle before Spring Cloud AWS, if you
introduce a static field to your unit test, annotated with `o.j.j.a.e.RegisterExtension`.
The lifecycle events you want to hook into are `BeforeAllCallback` and `AfterAllCallback`.

You can create a better LocalStackContainer, which exposes LocalStack through a fixed port,
which you can then configure into `src/test/resources/com/amazonaws/regions/override/regions.xml`
which is a magic file path which will be loaded by 
`c.a.r.LegacyRegionXmlMetadataBuilder.loadOverrideMetadataIfExists`
if it exists in the classpath.

Finally, Spring Cloud AWS uses the AWS Java SDK to read the instance configuration, in case
the application is running on a EC2 instance, and you've given that instance a role and attached
policies to that role, which allow that instance access to things such as S3 buckets.
Normally, this AWS API endpoint would generate some temporary keys according to those
policies, and serve them through a magic API endpoint. We'll want to hook into this
process in our JUnit 5 extension initialization, by setting the system property
`com.amazonaws.sdk.ec2MetadataServiceEndpointOverride` to our own mock endpoint URL.

# Lab notes

## How to figure out how the AWS client endpoint urls are set

Set a field modification watchpoint on `com.amazonaws.AmazonWebServiceClient.endpoint`.

## Where to hook into `regions.xml` file parsing

Add a file called `src/test/resources/com/amazonaws/regions/override/regions.xml`.

It will be loaded in `com.amazonaws.regions.LegacyRegionXmlMetadataBuilder.loadOverrideMetadataIfExists`.

Just add some nonsense to the file, and a stack trace will present itself.

The parser for `regions.xml` is `com.amazonaws.regions.RegionMetadataParser.internalParse`.



## Relevant issues and documentation:

https://github.com/spring-cloud/spring-cloud-aws/issues/570

https://github.com/aws/aws-sdk-java/issues/842

https://github.com/aws/aws-sdk-java/issues/1766

https://raw.githubusercontent.com/aws/aws-sdk-java/master/aws-java-sdk-core/src/main/resources/com/amazonaws/internal/config/awssdk_config_default.json

https://raw.githubusercontent.com/aws/aws-sdk-java/master/aws-java-sdk-core/src/test/resources/com/amazonaws/regions/fake-regions.xml

https://cloud.spring.io/spring-cloud-static/spring-cloud-aws/2.2.2.RELEASE/reference/html/

https://cloud.spring.io/spring-cloud-static/spring-cloud-aws/2.2.2.RELEASE/reference/html/appendix.html
