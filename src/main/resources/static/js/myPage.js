let username = "guest"

// 복습 우선 순위 데이터 삽입 (예시 데이터)
const tableBody = document.getElementById('priorityTable');
const rows = [
    { word: 'example', wrongCount: 10, correctRate: '50%' },
    { word: 'test', wrongCount: 5, correctRate: '70%' }
];
rows.forEach(row => {
    const tr = document.createElement('tr');
    tr.innerHTML = `<td>${row.word}</td><td>${row.wrongCount}</td><td>${row.correctRate}</td>`;
    tableBody.appendChild(tr);
});

$(document).ready(function (){
    get7dayTime();
    levelPie();
    $("#blank-nickname").text("🏠 " + username + " 🏠");
});

// 학습 시간과 풀이 시간
function get7dayTime() {
    $.ajax({
        type: "GET",
        url: `/my-page/time/${username}`,
        headers: {},
        data: {},
        contentType: false,
        processData: false,
        success: function (response) {
            const today = new Date(); // 오늘 날짜 계산
            const weekDays = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];

            // 오늘을 기준으로 시작 요일부터 요일 순서를 재정렬
            const startIndex = today.getDay(); // 오늘의 요일 인덱스 (0: Sun ~ 6: Sat)
            let studyData= Array(7).fill(0);
            let quizData= Array(7).fill(0);

            for(i=0; i<response.length; i++){
                const x = new Date(response[i].date).getDay(); // 요일 index
                studyData.splice(x,1,((response[i].study_time)/60).toFixed(1)); // 초->분 단위
                quizData.splice(x,1,((response[i].quiz_time)/60).toFixed(1));
            }

            // 오늘 날짜가 가장 마지막으로 띄우게 재정렬
            const orderedWeekDays = [...weekDays.slice(startIndex + 1), ...weekDays.slice(0, startIndex + 1)];
            studyData = [...studyData.slice(startIndex + 1), ...studyData.slice(0, startIndex + 1)];
            quizData = [...quizData.slice(startIndex + 1), ...quizData.slice(0, startIndex + 1)];

            // Chart.js 차트 생성
            const timeCtx = document.getElementById('timeChart').getContext('2d');
            new Chart(timeCtx, {
                type: 'bar',
                data: {
                    labels: orderedWeekDays,
                    datasets: [
                        {
                            label: '학습 시간 (분)',
                            data: studyData,
                            backgroundColor: 'rgba(75, 192, 192, 0.5)',
                        },
                        {
                            label: '풀이 시간 (분)',
                            data: quizData,
                            backgroundColor: 'rgba(255, 99, 132, 0.5)',
                        }
                    ]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    scales: {
                        x: {title: {display: true, text: '요일'}},
                        y: {title: {display: true, text: '시간 (분)'}, beginAtZero: true}
                    }
                }
            });
        }
    })
}

// Level별 학습 비율
function levelPie(){
    $.ajax({
        type: "GET",
        url: `/my-page/level/${username}`,
        headers: {},
        data: {},
        contentType: false,
        processData: false,
        success: function (response) {
            // reduce()를 활용해 전체 개수를 구한 뒤, 레벨별 데이터를 정렬하여 비율을 계산
            const total = response.reduce((sum, level) => sum + level.cnt, 0); // (sum : 누적 반환 변수, level : 처리할 현재 요소, 처리할 현재 요소의 인덱스)
            const sortedData = response.sort((a, b) => a.level - b.level);
            const percentages = sortedData.map((level) => ((level.cnt / total) * 100).toFixed(1));

            // 학습한 예문 level
            const additionalCtx = document.getElementById('additionalChart').getContext('2d');
            new Chart(additionalCtx, {
                type: 'pie',
                data: {
                    labels: ['Easy', 'Medium', 'Hard'],
                    datasets: [{
                        data: percentages,
                        backgroundColor: ['#FF9999', '#FFCC99', '#99CCFF']
                    }]
                },
                options: {
                    responsive: true,
                }
            });
        }
    })
}

function levelPieIMG(){
    const url = `http://127.0.0.1:5000/my-page/level/image/${username}`; // Flask 서버 URL
    $.ajax({
        type: "GET",
        url: url,
        xhrFields: {
            responseType: 'blob'
        },
        success: function (response) {
            const imgBlob = new Blob([response], { type: 'image/png' });
            const imgURL = URL.createObjectURL(imgBlob);

            const newWindow = window.open("", "_blank", "width=640,height=480");
            const imgElement = newWindow.document.createElement("img");
            imgElement.src = imgURL;
            imgElement.alt = "Level Pie Chart";

            newWindow.document.body.appendChild(imgElement);

            // window.URL.revokeObjectURL(imgURL);
        },
        error: function (err) {
            console.error("이미지를 로드하는 중 오류가 발생했습니다.");
        }
    })
}