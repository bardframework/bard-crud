package org.bardframework.form.table.utils;

import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Assertions;
import org.bardframework.commons.utils.ReflectionUtils;
import org.bardframework.form.FormTemplate;
import org.bardframework.form.field.FieldTemplate;
import org.bardframework.form.flow.FlowData;
import org.bardframework.form.table.TableTemplate;
import org.bardframework.form.table.TableUtils;
import org.bardframework.form.table.header.TableHeaderTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;

import java.lang.reflect.Field;
import java.util.*;

public class TableModeCheckUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(TableModeCheckUtils.class);

    public static void checkDefinitionValidity(TableTemplate template, FlowData flowData) {
        Assertions.assertThat(template.getModelClass()).withFailMessage("model class of [%s] table not set", template.getName()).isNotNull();
        for (TableHeaderTemplate<?, ?> headerTemplate : template.getHeaderTemplates()) {
            Assertions.assertThat(headerTemplate.getName()).withFailMessage("some headers name of table [%s] is empty.", template.getName()).isNotEmpty();
            try {
                ReflectionUtils.getGetterMethod(template.getModelClass(), headerTemplate.getName());
                //TODO check getter return type with header dataType
            } catch (NoSuchMethodException e) {
                LOGGER.error("error get getter of [{}.{}]", template.getModelClass(), headerTemplate.getName(), e);
                Assertions.fail(headerTemplate.getName() + " field not exist in " + template.getModelClass().getName());
            }
            String headerTitle = TableUtils.getHeaderStringValue(template, headerTemplate, "title", flowData.getLocale(), Map.of(), headerTemplate.getTitle());
            Assertions.assertThat(headerTitle).withFailMessage("header title [%s.%s] in locale [%s] is not set", template.getName(), headerTemplate.getName(), flowData.getLocale().getLanguage()).isNotEmpty();
        }
        TableModeCheckUtils.checkFormModelValidity(flowData, template, template.getFilterFormTemplate());
        TableModeCheckUtils.checkFormModelValidity(flowData, template, template.getSaveFormTemplate());
        TableModeCheckUtils.checkFormModelValidity(flowData, template, template.getUpdateFormTemplate());
    }

    private static void checkFormModelValidity(FlowData flowData, TableTemplate tableTemplate, FormTemplate formTemplate) {
        if (null == formTemplate) {
            return;
        }
        Assertions.assertThat(formTemplate.getDtoClass()).withFailMessage("dto class of [%s] forms not set", tableTemplate.getName()).isNotNull();
        try {
            ReflectionUtils.newInstance(formTemplate.getDtoClass());
        } catch (Exception e) {
            LOGGER.error("error instantiating class: " + formTemplate.getDtoClass(), e);
            Assertions.fail("can't instantiate class [%s], maybe default constructor not exist", formTemplate.getDtoClass());
        }
        for (FieldTemplate<?> formField : formTemplate.getFieldTemplates(flowData)) {
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

    public static List<String> checkI18nExistence(TableTemplate template, FlowData flowData) {
        List<String> notExistence = new ArrayList<>();
        if (TableModeCheckUtils.isNotExist(template.getTitle(), template.getMessageSource(), flowData.getLocale())) {
            notExistence.add(template.getTitle());
        }
        if (TableModeCheckUtils.isNotExist(template.getHint(), template.getMessageSource(), flowData.getLocale())) {
            notExistence.add(template.getHint());
        }
        template.getHeaderTemplates().forEach(header -> notExistence.addAll(TableModeCheckUtils.checkI18nExistence(header, template.getMessageSource(), flowData.getLocale())));

        notExistence.addAll(TableModeCheckUtils.checkI18nExistence(template.getFilterFormTemplate(), flowData));
        notExistence.addAll(TableModeCheckUtils.checkI18nExistence(template.getSaveFormTemplate(), flowData));
        notExistence.addAll(TableModeCheckUtils.checkI18nExistence(template.getUpdateFormTemplate(), flowData));
        return notExistence;
    }

    private static List<String> checkI18nExistence(FieldTemplate<?> template, MessageSource messageSource, Locale locale) {
        return new ArrayList<>();
    }

    private static List<String> checkI18nExistence(FormTemplate template, FlowData flowData) {
        if (null == template) {
            return Collections.emptyList();
        }
        List<String> notExistence = new ArrayList<>();
        if (TableModeCheckUtils.isNotExist(template.getTitle(), template.getMessageSource(), flowData.getLocale())) {
            notExistence.add(template.getTitle());
        }
        if (TableModeCheckUtils.isNotExist(template.getConfirmMessage(), template.getMessageSource(), flowData.getLocale())) {
            notExistence.add(template.getConfirmMessage());
        }
        if (TableModeCheckUtils.isNotExist(template.getSubmitLabel(), template.getMessageSource(), flowData.getLocale())) {
            notExistence.add(template.getSubmitLabel());
        }
        template.getFieldTemplates(flowData).forEach(field -> notExistence.addAll(TableModeCheckUtils.checkI18nExistence(field, template.getMessageSource(), flowData.getLocale())));
        return notExistence;
    }

    private static List<String> checkI18nExistence(TableHeaderTemplate<?, ?> headerTemplate, MessageSource messageSource, Locale locale) {
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
            LOGGER.debug("message key [{}] not exist for locale [{}]", key, locale);
            return true;
        }
    }
}
