package com.rosan.dhizuku.data.reflect.model.impl

import com.rosan.dhizuku.data.reflect.repo.ReflectRepo
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Method

class ReflectRepoImpl : ReflectRepo {
    override fun getConstructors(clazz: Class<*>): Array<Constructor<*>> = clazz.constructors
    override fun getDeclaredConstructors(clazz: Class<*>): Array<Constructor<*>> =
        clazz.declaredConstructors

    override fun getFields(clazz: Class<*>): Array<Field> = clazz.fields

    override fun getDeclaredFields(clazz: Class<*>): Array<Field> = clazz.declaredFields

    override fun getMethods(clazz: Class<*>): Array<Method> = clazz.methods

    override fun getDeclaredMethods(clazz: Class<*>): Array<Method> = clazz.declaredMethods

    override fun getConstructor(clazz: Class<*>, vararg parameterTypes: Class<*>): Constructor<*>? {
        for (constructor in getConstructors(clazz)) {
            val expectedTypes = constructor.parameterTypes
            if (expectedTypes.size != parameterTypes.size) continue
            for (i in expectedTypes.indices)
                if (expectedTypes[i] != parameterTypes[i]) continue
            return constructor
        }
        return null
    }

    override fun getDeclaredConstructor(
        clazz: Class<*>,
        vararg parameterTypes: Class<*>
    ): Constructor<*>? {
        for (constructor in getDeclaredConstructors(clazz)) {
            val expectedTypes = constructor.parameterTypes
            if (expectedTypes.size != parameterTypes.size) continue
            for (i in expectedTypes.indices)
                if (expectedTypes[i] != parameterTypes[i]) continue
            return constructor
        }
        return null
    }

    override fun getField(clazz: Class<*>, name: String): Field? {
        for (field in getFields(clazz)) {
            if (field.name != name) continue
            return field
        }
        return null
    }

    override fun getDeclaredField(clazz: Class<*>, name: String): Field? {
        for (field in getDeclaredFields(clazz)) {
            if (field.name != name) continue
            return field
        }
        return null
    }

    override fun getMethod(
        clazz: Class<*>,
        name: String,
        vararg parameterTypes: Class<*>
    ): Method? {
        for (method in getMethods(clazz)) {
            if (method.name != name) continue
            val expectedTypes = method.parameterTypes
            if (expectedTypes.size != parameterTypes.size) continue
            for (i in expectedTypes.indices)
                if (expectedTypes[i] != parameterTypes[i]) continue
            return method
        }
        return null
    }

    override fun getDeclaredMethod(
        clazz: Class<*>,
        name: String,
        vararg parameterTypes: Class<*>
    ): Method? {
        for (method in getDeclaredMethods(clazz)) {
            if (method.name != name) continue
            val expectedTypes = method.parameterTypes
            if (expectedTypes.size != parameterTypes.size) continue
            for (i in expectedTypes.indices)
                if (expectedTypes[i] != parameterTypes[i]) continue
            return method
        }
        return null
    }
}