package com.data2.salmon.core.engine.spring;

import com.data2.salmon.core.engine.enums.DataBase;
import com.data2.salmon.core.engine.inter.Mapper;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.util.SystemPropertyUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashSet;

/**
 * @author data2
 */
@Component
public class Scanner {
    public static final String DEFAULT_RESOURCE_PATTERN = "**/*.class";

    public static HashSet<Inner> loadCheckClassMethods(String scanPackages) {
        HashSet<Inner> set = Sets.newHashSet();
        String[] scanPackageArr = scanPackages.split(",");
        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(resourcePatternResolver);
        for (String basePackage : scanPackageArr) {
            if (StringUtils.isBlank(basePackage)) {
                continue;
            }
            String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
                    ClassUtils.convertClassNameToResourcePath(SystemPropertyUtils.resolvePlaceholders(basePackage)) + "/" + DEFAULT_RESOURCE_PATTERN;
            try {
                Resource[] resources = resourcePatternResolver.getResources(packageSearchPath);
                for (Resource resource : resources) {
                    //here resource is class
                    set.addAll(loadClassMethod(metadataReaderFactory, resource));
                }
            } catch (Exception e) {
            }

        }
        return set;
    }

    private static HashSet<Inner> loadClassMethod(MetadataReaderFactory metadataReaderFactory, Resource resource) throws IOException {
        HashSet<Inner> set = Sets.newHashSet();
        try {
            if (resource.isReadable()) {
                MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
                if (metadataReader != null) {
                    String className = metadataReader.getClassMetadata().getClassName();
                    try {
                        set.addAll(tryFieldInfo(className));
                    } catch (Exception e) {
                    }
                }
            }
        } catch (Exception e) {
        }
        return set;
    }

    private static HashSet<Inner> tryFieldInfo(String fullClassName) {
        HashSet<Inner> set = Sets.newHashSet();
        try {
            Class<?> clz = Class.forName(fullClassName);
            for (Field field : clz.getFields()) {
                if (field.isAnnotationPresent(Mapper.class)) {
                    Mapper m = field.getAnnotation(Mapper.class);
                    set.add(new Inner().setDatabase(m.database()).setFile(m.file()).setName(m.name()));
                }
            }
        } catch (Exception e) {
        }
        return set;
    }

    static class Inner {
        String name;
        String file;
        DataBase database;

        public Inner() {

        }

        public Inner setName(String name) {
            this.name = name;
            return this;
        }

        public Inner setFile(String file) {
            this.file = file;
            return this;
        }

        public Inner setDatabase(DataBase database) {
            this.database = database;
            return this;
        }

        @Override
        public String toString() {
            return name + "," + database + "," + file;
        }
    }
}
