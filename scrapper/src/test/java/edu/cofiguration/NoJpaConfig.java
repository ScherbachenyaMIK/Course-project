package edu.cofiguration;

import edu.service.ArticlesService;
import edu.service.ImagesService;
import edu.service.UsersService;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@TestConfiguration
@EnableAutoConfiguration(exclude = {
        HibernateJpaAutoConfiguration.class,
        JpaRepositoriesAutoConfiguration.class,
        DataSourceAutoConfiguration.class
})
@ComponentScan(
        basePackages = {"edu.service", "edu.model"},
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "edu\\.model\\.db\\..*"),
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "edu\\.service\\..*Service")
        }
)
public class NoJpaConfig {
    @MockBean
    private UsersService usersService;
    @MockBean
    private ImagesService imagesService;
    @MockBean
    private ArticlesService articlesService;
}
