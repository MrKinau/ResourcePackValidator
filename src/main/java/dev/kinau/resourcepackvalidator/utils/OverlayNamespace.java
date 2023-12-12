package dev.kinau.resourcepackvalidator.utils;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.io.File;
import java.util.List;

@RequiredArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
public class OverlayNamespace {
    private final File overlayOrRootDir;
    private final File assetsDir;
    private final Overlay overlay;
    private final List<OverlayNamespace> underlyingOverlays;

    public String getName() {
        if (overlay == null)
            return assetsDir.getName();
        return overlay.getName() + "/" + assetsDir.getName();
    }

    public String getNamespaceName() {
        return assetsDir.getName();
    }
}
