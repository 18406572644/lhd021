package com.community.idle.common;

import com.community.idle.entity.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class EntityConverter {

    public static User convertUser(User user) {
        if (user == null) return null;
        user.setPublishCount(user.getReleaseCount() != null ? user.getReleaseCount() : 0);
        user.setClaimCount(0);
        user.setRoleName(StatusConverter.getRoleName(user.getRole()));
        user.setStatusName(StatusConverter.getUserStatus(user.getStatus()));
        if (user.getNickname() == null || user.getNickname().isEmpty()) {
            user.setNickname(user.getUsername());
        }
        if (user.getAvatar() == null) {
            user.setAvatar("");
        }
        return user;
    }

    public static List<User> convertUserList(List<User> list) {
        if (list == null) return null;
        list.forEach(EntityConverter::convertUser);
        return list;
    }

    public static IdleItem convertIdleItem(IdleItem item) {
        if (item == null) return null;
        item.setName(item.getItemName());
        item.setDescription(item.getItemDesc());
        item.setImageUrl(item.getItemImages());
        item.setItemCondition(StatusConverter.getCondition(item.getConditionLevel()));
        item.setPublisherName(item.getUsername());
        item.setStatusText(StatusConverter.getItemStatus(item.getStatus()));
        item.setStatusValue(item.getStatus());
        item.setExpectedExchange("");
        item.setContactPhone("");
        if (item.getOfflineTime() != null && item.getStatus() == 3) {
            item.setOfflineDays(Duration.between(item.getOfflineTime(), LocalDateTime.now()).toDays());
        } else {
            item.setOfflineDays(0L);
        }
        return item;
    }

    public static List<IdleItem> convertIdleItemList(List<IdleItem> list) {
        if (list == null) return null;
        list.forEach(EntityConverter::convertIdleItem);
        return list;
    }

    public static ExchangeApply convertExchangeApply(ExchangeApply apply) {
        if (apply == null) return null;
        return apply;
    }

    public static List<ExchangeApply> convertExchangeApplyList(List<ExchangeApply> list) {
        if (list == null) return null;
        list.forEach(EntityConverter::convertExchangeApply);
        return list;
    }

    public static ClaimRecord convertClaimRecord(ClaimRecord record) {
        if (record == null) return null;
        return record;
    }

    public static List<ClaimRecord> convertClaimRecordList(List<ClaimRecord> list) {
        if (list == null) return null;
        list.forEach(EntityConverter::convertClaimRecord);
        return list;
    }

    public static PickupPoint convertPickupPoint(PickupPoint point) {
        if (point == null) return null;
        return point;
    }

    public static List<PickupPoint> convertPickupPointList(List<PickupPoint> list) {
        if (list == null) return null;
        list.forEach(EntityConverter::convertPickupPoint);
        return list;
    }

    public static CreditRating convertCreditRating(CreditRating rating) {
        if (rating == null) return null;
        return rating;
    }

    public static List<CreditRating> convertCreditRatingList(List<CreditRating> list) {
        if (list == null) return null;
        list.forEach(EntityConverter::convertCreditRating);
        return list;
    }

    public static ItemArchive convertItemArchive(ItemArchive archive) {
        if (archive == null) return null;
        return archive;
    }

    public static List<ItemArchive> convertItemArchiveList(List<ItemArchive> list) {
        if (list == null) return null;
        list.forEach(EntityConverter::convertItemArchive);
        return list;
    }

    public static MonthlyStatistics convertMonthlyStatistics(MonthlyStatistics stats) {
        if (stats == null) return null;
        return stats;
    }

    public static List<MonthlyStatistics> convertMonthlyStatisticsList(List<MonthlyStatistics> list) {
        if (list == null) return null;
        list.forEach(EntityConverter::convertMonthlyStatistics);
        return list;
    }
}
