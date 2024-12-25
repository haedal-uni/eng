// 파일 업로드
function uploadFile(input) {
    let file = input.files[0];
    if (!file) return;

    let formData = new FormData();
    formData.append('file', file);

    // AJAX 파일 업로드
    fetch('/upload-excel', {
        method: 'POST',
        body: formData
    })
        .then(response => {
            if (response.ok) {
                return "upload success";
            }
            throw new Error('File upload failed');
        })
        .then(message => alert(message))
        .catch(error => alert(error.message));
}

function saveTime(startTime, endTime, status){
    let time = Math.floor((endTime-startTime)/1000);
    let data = {"username":username, "time":time, "status":status}
    console.log("time : " + time)
    $.ajax({
        type: "POST",
        url: `/my-page`,
        headers: {},
        data: JSON.stringify(data),
        contentType: 'application/json',
        processData: false,
        success: function (response) {
        }
    })
}