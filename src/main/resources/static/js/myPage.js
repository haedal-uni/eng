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

function get7dayTime() {
    $.ajax({
        type: "GET",
        url: `/my-page/time/${username}`,
        headers: {},
        data: {},
        contentType: false,
        processData: false,
        success: function (response) {
            // 오늘 날짜 계산
            const today = new Date();

            // 요일 이름 배열
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

function sortDate(list){
    return list.sort(function (a, b) {
        return new Date(a.date) - new Date(b.date);
    });
}

function levelPie(){
    $.ajax({
        type: "GET",
        url: `/my-page/level/${username}`,
        headers: {},
        data: {},
        contentType: false,
        processData: false,
        success: function (response) {
            const sum = response[0].cnt+response[1].cnt + response[2].cnt;
            response.sort( (prev, curr) => {
                if(prev.level>curr.level) return 1;
                if(prev.level<curr.level) return -1;
            })
            const count = response.map(function (e) {
                return (e.cnt/sum*100).toFixed(1);
            })

            // 학습한 예문 level
            const additionalCtx = document.getElementById('additionalChart').getContext('2d');
            new Chart(additionalCtx, {
                type: 'pie',
                data: {
                    labels: ['Easy', 'Medium', 'Hard'],
                    datasets: [{
                        data: count,
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