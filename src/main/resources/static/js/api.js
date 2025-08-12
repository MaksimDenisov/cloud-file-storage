function deleteFile(filepath) {
    const token = document.querySelector('meta[name="_csrf"]').content;
    const header = document.querySelector('meta[name="_csrf_header"]').content;

    const formData = new FormData();
    formData.append("filepath", filepath);
    console.log('Delete file with path : ' + filepath);
    fetch("/delete-file", {
        method: "POST",
        headers: {
            [header]: token
        },
        body: formData
    })
        .then(response => {
            if (response.redirected) {
                window.location.href = response.url;
            } else {
                console.log("File deleted without redirect");
            }
        })
        .catch(err => console.error("Error deleting file:", err));
}

function deleteFolder(folderPath) {
    const token = document.querySelector('meta[name="_csrf"]').content;
    const header = document.querySelector('meta[name="_csrf_header"]').content;

    const formData = new FormData();
    formData.append("folder-path", folderPath);
    console.log('Delete folder with path : ' + folderPath);
    fetch("/delete-folder", {
        method: "POST",
        headers: {
            [header]: token
        },
        body: formData
    })
        .then(response => {
            if (response.redirected) {
                window.location.href = response.url;
            } else {
                console.log("Folder deleted without redirect");
            }
        })
        .catch(err => console.error("Error deleting folder:", err));
}