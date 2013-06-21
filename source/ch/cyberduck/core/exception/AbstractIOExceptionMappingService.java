package ch.cyberduck.core.exception;

import ch.cyberduck.core.Path;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.threading.BackgroundException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.text.MessageFormat;

/**
 * @version $Id$
 */
public abstract class AbstractIOExceptionMappingService<T extends Exception> implements IOExceptionMappingService<T> {
    private static Logger log = Logger.getLogger(AbstractIOExceptionMappingService.class);

    public BackgroundException map(final String message, final T failure) {
        final BackgroundException exception = this.map(failure);
        exception.setMessage(StringUtils.chomp(Locale.localizedString(message, "Error")));
        return exception;
    }

    public BackgroundException map(final String message, final T failure, final Path directory) {
        final BackgroundException exception = this.map(failure);
        exception.setPath(directory);
        exception.setMessage(MessageFormat.format(StringUtils.chomp(Locale.localizedString(message, "Error")), directory.getName()));
        return exception;
    }

    /**
     * @param exception Service error
     * @return Mapped exception
     */
    @Override
    public abstract BackgroundException map(T exception);

    protected StringBuilder append(final StringBuilder buffer, final String message) {
        if(StringUtils.isBlank(message)) {
            return buffer;
        }
        if(buffer.length() > 0) {
            buffer.append(" ");
        }
        buffer.append(message);
        if(buffer.charAt(buffer.length() - 1) == '.') {
            return buffer;
        }
        return buffer.append(".");
    }

    protected BackgroundException wrap(final T e, final StringBuilder buffer) {
        final BackgroundException exception = new BackgroundException(buffer.toString(), e);
        exception.setMessage(Locale.localizedString("Connection failed"));
        return exception;
    }
}
