package edu.service;

import edu.model.web.DTO;
import edu.model.web.dto.ArticleInformationDTO;
import edu.model.web.dto.ArticlePreviewDTO;
import edu.model.web.request.ArticlesForFeedRequest;
import java.net.URI;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.stereotype.Service;

@SuppressWarnings({"MultipleStringLiterals", "MagicNumber"})
@Service
public class GetRequestsHandler {
    public List<DTO> handleFindArticlesRequest(ArticlesForFeedRequest request) {
        return List.of(
                new ArticlePreviewDTO(
                        URI.create("/resources/standard_icon.png"),
                        "Author 1",
                        "Title 1",
                        new ArticleInformationDTO(
                                "#Programming",
                                "Programming",
                                30,
                                ZonedDateTime.ofInstant(
                                        Instant.now().minus(1, ChronoUnit.DAYS),
                                        ZoneId.systemDefault()
                                ),
                                "Finished",
                                15,
                                5,
                                5
                        ),
                        URI.create("/resources/standard_preview.png"),
                        """
                                Когда компании начинают планировать внедрение информационной системы
                                или автоматизацию нового процесса, перед ними встает важный вопрос –
                                как это реализовать? Выбор подходов зависит от многих факторов: есть
                                ли сформулированные цели, объем неопределенности в бизнес-процессах,
                                принятые в компании стандарты и ряд других факторов. Чтобы внедрение
                                прошло максимально эффективно, и результат проекта удовлетворил
                                заказчика можно, и даже нужно, выбирать разный подход, методологию,
                                свой набор необходимых шагов.
                                <br><br>
                                В этой статье мы на основе своего опыта реализации проектов расскажем
                                о вариантах и шагах для достижения цели на примере внедрения решений
                                на платформе ELMA365.
                                """,
                        URI.create("/articles/article1")
                ),
                new ArticlePreviewDTO(
                        URI.create("/resources/standard_icon.png"),
                        "Author 2",
                        "Title 2",
                        new ArticleInformationDTO(
                                "#Science",
                                "Science",
                                45,
                                ZonedDateTime.ofInstant(
                                        Instant.now().minus(10, ChronoUnit.DAYS),
                                        ZoneId.systemDefault()
                                        ),
                                "Finished",
                                50,
                                25,
                                15
                        ),
                        URI.create("/resources/standard_preview.png"),
                        """
                                «Красота в простате» (орфография сохранена), — как хорошо заметил
                                однажды один из персонажей интернета, и это безусловно достойное
                                высказывание разлетелось по всем уголкам сети.
                                <br><br>
                                Тем не менее, как ни странно, он прав: если и не в части орфографии,
                                то хотя бы во вложенном смысле. Чем система проще, тем легче её
                                создать (а также она надёжнее и т. д., но нас интересует именно
                                лёгкость реализации).
                                <br><br>
                                А что, если я скажу вам, что существует несколько весьма простых
                                способов изучения простейших, а также как минимум один весьма лёгкий
                                способ превратить смартфон в микроскоп для рассматривания маркировок
                                электронных деталей и не только?
                                """,
                        URI.create("/articles/article2")
                )
        );
    }
}
