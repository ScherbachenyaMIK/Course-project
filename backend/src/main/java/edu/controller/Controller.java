package edu.controller;

import edu.model.ArticlePreviewDTO;
import java.util.List;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
public class Controller {
    @SuppressWarnings({"LineLength", "MultipleStringLiterals", "RegexpSingleline"})
    @GetMapping("/")
    public ModelAndView getHome(Model model) {
        List<ArticlePreviewDTO> articles = List.of(
                new ArticlePreviewDTO(
                        "Автор 1",
                        "Название статьи 1",
                        "Развлечения, 50 минут",
                        "/resources/standard-preview.png",
                        """
                                В течение нескольких дней я потратил значительную часть своего времени на то, чтобы опробовать новый китайский ИИ-чатбот Deepseek R-1. За последние несколько дней он привлек к себе много внимания, и на то есть веские причины: чатбот действительно способный - иногда даже лучше, чем ChatGPT. И он дешевый. Очень дешевый.
                                <br><br>                           
                                Несмотря на то, что он появился относительно недавно, он уже успел зарекомендовать себя в сфере ИИ как рассуждающая модель с открытым исходным кодом. По многим показателям производительность находится на одном уровне с моделью o1 от OpenAI, а стоимость постоянного использования чата и API значительно ниже, чем у конкурентов.
                                <br><br>                              
                                Как человек, который любит пробовать новейшие ИИ-инструменты, я сразу же приступил к работе и пользовался Deepseek R-1 в течение нескольких дней. Удивительно, но он ни разу не завис, не тормозил и, что еще более удивительно, ни разу не попросил меня купить подписку и не сказал, что я превысил свой ежедневный лимит использования.
                                """,
                        "/articles/1"),
                new ArticlePreviewDTO(
                        "Автор 2",
                        "Название статьи 2",
                        "Развлечения, 20 минут",
                        "/resources/standard-preview.png",
                        "Краткая выдержка 2",
                        "/articles/2"),
                new ArticlePreviewDTO("Автор 3",
                        "Название статьи 3",
                        "Развлечения, 30 минут",
                        "/resources/standard-preview.png",
                        "Краткая выдержка 3",
                        "/articles/3")
        );
        model.addAttribute("articles", articles);
        return new ModelAndView("Home");
    }
}
