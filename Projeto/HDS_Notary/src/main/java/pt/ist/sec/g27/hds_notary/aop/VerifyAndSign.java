package pt.ist.sec.g27.hds_notary.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface VerifyAndSign {
    /**
     * @return if it is to verify and sign notary pass true, otherwise false
     */
    boolean value() default false;
}
