const dropArea = document.getElementById("drop-area");
const currentPath = "[[${currentPath}]]";

dropArea.addEventListener("dragover", (event) => {
    event.preventDefault();
    dropArea.style.background = "#e3f2fd";
});

dropArea.addEventListener("dragleave", () => {
    dropArea.style.background = "#f8f9fa";
});

function readDirectory(entry, path = "") {
    return new Promise((resolve) => {
        let files = [];
        if (entry.isFile) {
            entry.file((file) => {
                file.relativePath = path + file.name;
                files.push(file);
                resolve(files);
            });
        } else if (entry.isDirectory) {
            let reader = entry.createReader();
            reader.readEntries(async (entries) => {
                let promises = entries.map((childEntry) =>
                    readDirectory(childEntry, path + entry.name + "/")
                );
                let results = await Promise.all(promises);
                resolve(results.flat());
            });
        }
    });
}

document.getElementById("drop-area").addEventListener("drop", async (event) => {
    event.preventDefault();
    let items = event.dataTransfer.items;
    let formData = new FormData();
    formData.append("path", currentPath);
    for (let item of items) {
        let entry = item.webkitGetAsEntry();
        if (entry) {
            let files = await readDirectory(entry);
            files.forEach((file) => formData.append("files", file, file.relativePath));
        }
    }
    fetch("/upload-folder", {
        method: "POST",
        body: formData
    }).then(() => {
        window.location.reload();
    });
});