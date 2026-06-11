// 파일 업로드
function uploadFile(input) {
  let file = input.files[0];
  if (!file) return;

  let formData = new FormData();
  formData.append('file', file);

  fetch('/upload-excel', {
    method: 'POST',
    body: formData
  })
    .then(response => {
      if (response.ok) return "upload success";
      throw new Error('File upload failed');
    })
    .then(message => {
      Swal.fire({ icon: 'success', title: '업로드 완료', text: message, confirmButtonColor: '#ff6f61' });
    })
    .catch(error => {
      Swal.fire({ icon: 'error', title: '오류', text: error.message, confirmButtonColor: '#ff6f61' });
    });
}

function saveTime(startTime, endTime, status) {
  let time = Math.floor((endTime - startTime) / 1000);
  let data = { "username": username, "time": time, "status": status };
  $.ajax({
    type: "POST",
    url: `/my-page`,
    data: JSON.stringify(data),
    contentType: 'application/json',
    processData: false,
  });
}
