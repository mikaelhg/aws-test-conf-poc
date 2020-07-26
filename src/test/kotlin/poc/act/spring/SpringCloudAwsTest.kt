package poc.act.spring

import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTestContextBootstrapper
import org.springframework.test.context.BootstrapWith
import org.springframework.test.context.junit.jupiter.SpringExtension
import poc.act.junit.MockAmazonWebServices
import java.lang.annotation.Inherited


@kotlin.annotation.Target(AnnotationTarget.CLASS)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Inherited
@BootstrapWith(SpringBootTestContextBootstrapper::class)
@ExtendWith(value = [MockAmazonWebServices::class, SpringExtension::class])
annotation class SpringCloudAwsTest
