package edu.util;

import edu.model.db.entity.User;
import edu.model.web.dto.UserDTO;
import java.time.ZoneId;

@SuppressWarnings("HideUtilityClassConstructor")
public class UserDTOEntityConverter {
    public static UserDTO convert(User user) {
        UserDTO.UserDTOBuilder builder = UserDTO.builder();

        builder.id(user.getId());
        builder.username(user.getUsername());
        builder.nativeName(user.getName());
        builder.email(user.getEmail());
        builder.sex(user.getSex());
        builder.birthDate(
                user.getBirthDate() != null
                        ? user.getBirthDate().atZone(ZoneId.systemDefault())
                        : null
                );
        builder.registrationDate(user.getRegistrationDate().atZone(ZoneId.systemDefault()));
        builder.role(user.getUserRole());
        builder.description(user.getDescription());
        builder.articles(ArticlePreviewDTOEntityConverter.convert(user.getArticles()));

        return builder.build();
    }

    public static UserDTO emptyDTO() {
        UserDTO.UserDTOBuilder builder = UserDTO.builder();

        return builder.build();
    }
}
