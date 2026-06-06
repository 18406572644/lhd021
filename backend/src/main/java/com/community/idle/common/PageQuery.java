package com.community.idle.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PageQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    @Min(value = 1, message = "页码最小为1")
    private Integer pageNum = 1;

    @Min(value = 1, message = "每页条数最小为1")
    @Max(value = 100, message = "每页条数最大为100")
    private Integer pageSize = 10;

    private String orderByColumn;

    private String isAsc = "asc";

    public <T> IPage<T> buildPage() {
        Page<T> page = new Page<>(pageNum, pageSize);
        if (orderByColumn != null && !orderByColumn.isEmpty()) {
            String column = camelToUnderline(orderByColumn);
            if ("desc".equalsIgnoreCase(isAsc)) {
                page.addOrder(OrderItem.desc(column));
            } else {
                page.addOrder(OrderItem.asc(column));
            }
        }
        return page;
    }

    public <T> IPage<T> buildPage(List<OrderItem> orders) {
        Page<T> page = new Page<>(pageNum, pageSize);
        if (orders != null && !orders.isEmpty()) {
            page.addOrder(orders);
        }
        return page;
    }

    private String camelToUnderline(String str) {
        if (str == null || str.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (Character.isUpperCase(c)) {
                sb.append('_').append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
