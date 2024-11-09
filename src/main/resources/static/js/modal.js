var exampleModal = document.getElementById('rename-folder')
exampleModal.addEventListener('show.bs.modal', function (event) {
    // Button that triggered the modal
    var button = event.relatedTarget
    // Extract info from data-bs-* attributes
    var oldName = button.getAttribute('data-bs-name')
    // If necessary, you could initiate an AJAX request here
    // and then do the updating in a callback.
    //
    // Update the modal's content.
    var folderNameInput = exampleModal.querySelector('#folder-name')
    var Path = exampleModal.querySelector('#old-folder-path')

    //folderNameInput.textContent = oldName
    folderNameInput.value = oldName
})