package com.kodcu.service.ui;

import com.kodcu.other.Item;
import com.kodcu.service.FileWatchService;
import com.kodcu.service.PathResolverService;
import com.kodcu.service.ThreadService;
import javafx.application.Platform;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.StreamSupport;

/**
 * Created by usta on 12.07.2014.
 */
@Component
public class FileBrowseService {

    @Autowired
    private ThreadService threadService;

    @Autowired
    private PathResolverService pathResolver;

    @Autowired
    private AwesomeService awesomeService;

    @Autowired
    private FileWatchService watchService;

    private TreeItem<Item> rootItem;

    public void browse(final TreeView<Item> treeView, final Path browserPath) {

        threadService.runTaskLater(task -> {
            watchService.registerWatcher(treeView, browserPath, this::browse);
        });

        Platform.runLater(() -> {

            rootItem = new TreeItem<>(new Item(browserPath, String.format("Workdir (%s)", browserPath)), awesomeService.getIcon(browserPath));
            rootItem.setExpanded(true);
            try {
                StreamSupport
                        .stream(Files.newDirectoryStream(browserPath).spliterator(), false)
                        .sorted()
                        .forEach(path -> {
                            addToTreeView(path);
                        });
            } catch (final IOException e) {
                e.printStackTrace();
            }

            treeView.setRoot(rootItem);
        });
    }

    private void addToTreeView(Path path) {

        if (pathResolver.isHidden(path))
            return;

        if (pathResolver.isViewable(path)) {

            TreeItem<Item> treeItem = new TreeItem<>(new Item(path), awesomeService.getIcon(path));
            rootItem.getChildren().add(treeItem);
        }

    }

}
