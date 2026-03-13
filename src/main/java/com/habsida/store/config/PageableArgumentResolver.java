package com.habsida.store.config;

import org.springframework.core.MethodParameter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.List;
import java.util.stream.Stream;

/**
 * Resolves Pageable from request params: page, size, sort (e.g. sort=name,asc).
 */
public class PageableArgumentResolver implements HandlerMethodArgumentResolver {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 500;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return Pageable.class.equals(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        String pageParam = webRequest.getParameter("page");
        String sizeParam = webRequest.getParameter("size");
        String sortParam = webRequest.getParameter("sort");
        int page = parsePositiveInt(pageParam, DEFAULT_PAGE);
        int size = parsePositiveInt(sizeParam, DEFAULT_SIZE);
        if (size > MAX_SIZE) {
            size = MAX_SIZE;
        }
        Sort sort = parseSort(sortParam);
        return sort != null && sort.isSorted()
                ? PageRequest.of(page, size, sort)
                : PageRequest.of(page, size);
    }

    private static int parsePositiveInt(String value, int defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            int n = Integer.parseInt(value.trim());
            return n >= 0 ? n : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static Sort parseSort(String sortParam) {
        if (sortParam == null || sortParam.isBlank()) {
            return Sort.unsorted();
        }
        List<Sort.Order> orders = Stream.of(sortParam.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(PageableArgumentResolver::parseOrder)
                .filter(o -> o != null)
                .toList();
        return orders.isEmpty() ? Sort.unsorted() : Sort.by(orders);
    }

    private static Sort.Order parseOrder(String part) {
        if (part.startsWith("-")) {
            return new Sort.Order(Sort.Direction.DESC, part.substring(1).trim());
        }
        if (part.contains(",")) {
            int i = part.lastIndexOf(',');
            String prop = part.substring(0, i).trim();
            String dirStr = part.substring(i + 1).trim();
            Sort.Direction dir = "desc".equalsIgnoreCase(dirStr) ? Sort.Direction.DESC : Sort.Direction.ASC;
            return new Sort.Order(dir, prop);
        }
        return new Sort.Order(Sort.Direction.ASC, part);
    }
}
