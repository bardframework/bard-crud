package org.bardframework.table;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Assertions;
import org.bardframework.commons.utils.ReflectionUtils;
import org.bardframework.form.FormTemplate;
import org.bardframework.form.field.FieldTemplate;
import org.bardframework.table.header.HeaderTemplate;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;

import java.lang.reflect.Field;
import java.util.*;

@Slf4j
public class TableModeCheckUtils {

    public static void checkDefinitionValidity(TableTemplate template, Map<String, String> args, Locale locale) {
        Assertions.assertThat(template.getModelClass()).withFailMessage("model class of [%s] table not set", template.getName()).isNotNull();
        for (HeaderTemplate<?, ?> headerTemplate : template.getHeaderTemplates()) {
            Assertions.assertThat(headerTemplate.getName()).withFailMessage("some headers name of table [%s] is empty.", template.getName()).isNotEmpty();
            try {
                ReflectionUtils.getGetterMethod(template.getModelClass(), headerTemplate.getName());
                //TODO check getter return type with header dataType
            } catch (NoSuchMethodException e) {
                log.error("error get getter of [{}.{}]", template.getModelClass(), headerTemplate.getName(), e);
                Assertions.fail(headerTemplate.getName() + " field not exist in " + template.getModelClass().getName());
            }
            String headerTitle = TableUtils.getHeaderStringValue(template, headerTemplate, "title", locale, Map.of(), headerTemplate.getTitle());
            Assertions.assertThat(headerTitle).withFailMessage("header title [%s.%s] in locale [%s] is not set", template.getName(), headerTemplate.getName(), locale.getLanguage()).isNotEmpty();
        }
        TableModeCheckUtils.checkFormModelValidity(args, template, template.getFilterFormTemplate());
        TableModeCheckUtils.checkFormModelValidity(args, template, template.getSaveFormTemplate());
        TableModeCheckUtils.checkFormModelValidity(args, template, template.getUpdateFormTemplate());
    }

    private static void checkFormModelValidity(Map<String, String> args, TableTemplate tableTemplate, FormTemplate formTemplate) {
        if (null == formTemplate) {
            return;
        }
        Assertions.assertThat(formTemplate.getDtoClass()).withFailMessage("dto class of [%s] forms not set", tableTemplate.getName()).isNotNull();
        try {
            ReflectionUtils.newInstance(formTemplate.getDtoClass());
        } catch (Exception e) {
            log.error("error instantiating class: " + formTemplate.getDtoClass(), e);
            Assertions.fail("can't instantiate class [%s], maybe default constructor not exist", formTemplate.getDtoClass());
        }
        for (FieldTemplate<?> formField : formTemplate.getFieldTemplates(args)) {
            TableModeCheckUtils.checkSetter(formTemplate.getDtoClass(), formField.getName());
        }
    }

    private static void checkSetter(Class<?> clazz, String property) {
        Field field = ReflectionUtils.findField(clazz, property);
        if (null == field) {
            Assertions.fail(property + " field not exist in " + clazz.getName());
        }
        //TODO check setter arg by field type (String, Integer, LocalDate, etc)
    }

    public static List<String> checkI18nExistence(TableTemplate template, Map<String, String> args, Locale locale) {
        List<String> notExistence = new ArrayList<>();
        if (TableModeCheckUtils.isNotExist(template.getTitle(), template.getMessageSource(), locale)) {
            notExistence.add(template.getTitle());
        }
        if (TableModeCheckUtils.isNotExist(template.getDescription(), template.getMessageSource(), locale)) {
            notExistence.add(template.getDescription());
        }
        template.getHeaderTemplates().forEach(header -> notExistence.addAll(TableModeCheckUtils.checkI18nExistence(header, template.getMessageSource(), locale)));

        notExistence.addAll(TableModeCheckUtils.checkI18nExistence(template.getFilterFormTemplate(), args, locale));
        notExistence.addAll(TableModeCheckUtils.checkI18nExistence(template.getSaveFormTemplate(), args, locale));
        notExistence.addAll(TableModeCheckUtils.checkI18nExistence(template.getUpdateFormTemplate(), args, locale));
        return notExistence;
    }

    private static List<String> checkI18nExistence(FieldTemplate<?> template, MessageSource messageSource, Locale locale) {
        return new ArrayList<>();
    }

    private static List<String> checkI18nExistence(FormTemplate template, Map<String, String> args, Locale locale) {
        if (null == template) {
            return Collections.emptyList();
        }
        List<String> notExistence = new ArrayList<>();
        template.getFieldTemplates(args).forEach(field -> notExistence.addAll(TableModeCheckUtils.checkI18nExistence(field, template.getMessageSource(), locale)));
        return notExistence;
    }

    private static List<String> checkI18nExistence(HeaderTemplate<?, ?> headerTemplate, MessageSource messageSource, Locale locale) {
        List<String> notExistence = new ArrayList<>();
        if (TableModeCheckUtils.isNotExist(headerTemplate.getTitle(), messageSource, locale)) {
            notExistence.add(headerTemplate.getTitle());
        }
        return notExistence;
    }

    private static boolean isNotExist(String key, MessageSource messageSource, Locale locale) {
        if (null == key) {
            return false;
        }
        try {
            String message = messageSource.getMessage(key, new Object[0], locale);
            return StringUtils.isEmpty(message);
        } catch (NoSuchMessageException e) {
            log.debug("message key [{}] not exist for locale [{}]", key, locale);
            return true;
        }
    }
}
