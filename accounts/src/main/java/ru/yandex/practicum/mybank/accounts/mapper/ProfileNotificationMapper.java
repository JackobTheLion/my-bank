package ru.yandex.practicum.mybank.accounts.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.yandex.practicum.mybank.accounts.model.ProfileNotification;
import ru.yandex.practicum.mybank.commons.dto.NotificationRequest;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProfileNotificationMapper {

    @Mapping(target = "type", constant = "PROFILE_UPDATED")
    NotificationRequest toRequest(ProfileNotification notification);
}
