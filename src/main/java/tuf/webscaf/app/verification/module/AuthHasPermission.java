package tuf.webscaf.app.verification.module;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuthHasPermission {
    String value();
}