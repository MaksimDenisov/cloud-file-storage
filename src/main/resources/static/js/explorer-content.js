/* global bootstrap */
function initDeleteHandlers(elementId) {
    /**
     * Delete file button
     */
    document.getElementById(elementId).addEventListener('click', function (event) {
        const button = event.target.closest('button')
        if (!button) return;

        if (button.classList.contains('button-file-delete')) {
            const filepath = button.dataset.filepath;
            deleteFile(filepath);
        }
    });

    /**
     * Delete folder button
     */
    document.getElementById(elementId).addEventListener('click', function (event) {
        const button = event.target.closest('button')
        if (!button) return;

        if (button.classList.contains('button-folder-delete')) {
            const folderPath = button.dataset.folderpath;
            deleteFolder(folderPath);
        }
    });
}

function initRenameHandlers(elementId) {
    /** @type {import('bootstrap').Modal} */
    const renameFileModal = new bootstrap.Modal(document.getElementById("rename-file"));
    const inputFilePath = document.getElementById("rename-file-path");
    const inputNewFileName = document.getElementById("new-file-name");

    const renameFolderModal = new bootstrap.Modal(document.getElementById("rename-folder"));
    const inputFolderPath = document.getElementById("rename-folder-path");
    const inputNewFolderName = document.getElementById("new-folder-name");

    /**
     * Rename file button
     */
    document.getElementById(elementId).addEventListener('click', function (event) {
        const button = event.target.closest('button')
        if (!button) return;

        if (button.classList.contains('button-rename-file')) {
            const filePath = button.getAttribute("data-path");
            const newFileName = button.getAttribute("data-name");
            inputFilePath.value = filePath;
            inputNewFileName.value = newFileName;

            renameFileModal.show();
        }
    });

    /**
     * Rename folder button
     */
    document.getElementById(elementId).addEventListener('click', function (event) {
        const button = event.target.closest('button')
        if (!button) return;

        if (button.classList.contains('button-rename-folder')) {
            const folderPath = button.getAttribute("data-path");
            const newFolderName = button.getAttribute("data-name");

            inputFolderPath.value = folderPath;
            inputNewFolderName.value = newFolderName;

            renameFolderModal.show();
        }
    });
}

initDeleteHandlers('filesTable');
initDeleteHandlers('filesCards');

initRenameHandlers('filesTable');
initRenameHandlers('filesCards');