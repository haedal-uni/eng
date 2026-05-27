let username = "guest";

// 복습 우선 순위 테이블 채우기
function getRandomQuiz() {

    $.ajax({
        type: "GET",
        url: `/my-page/review/${username}`,
        contentType: false,
        processData: false,
        success: function (response) {
            const tableBody = document.getElementById('priorityTable');
            const rows = [
                {
                    word: response[0]['word'],
                    wrongCount: response[0]['wrongCount'],
                    correctRate: response[0]['correctRate']
                },
                {
                    word: response[1]['word'],
                    wrongCount: response[1]['wrongCount'],
                    correctRate: response[1]['correctRate']
                },
                {
                    word: response[2]['word'],
                    wrongCount: response[2]['wrongCount'],
                    correctRate: response[2]['correctRate']
                },
            ];
            rows.forEach(row => {
                const tr = document.createElement('tr');
                tr.className = 'border-b border-gray-100 hover:bg-vio-pale transition-colors';
                tr.innerHTML = `
        <td class="px-3 py-2 font-bold text-vio-deep">${row.word}</td>
        <td class="px-3 py-2 text-red-500">${row.wrongCount}회</td>
        <td class="px-3 py-2">${Math.round(row.correctRate)}%</td>
    `;
                tableBody.appendChild(tr);
            });

        }
    });
}

// 최근 오답 기록
function getRecentWrongQuiz() {
    $.ajax({
        type: "GET",
        url: `/my-page/recent-quiz/${username}`,
        contentType: false,
        processData: false,
        success: function (response) {
            console.log(response)
            const quizLogTable = document.getElementById('quizLogTable');

            quizLogTable.innerHTML = '';

            response.forEach((item) => {
                const isGrammar = item.quizType === 'grammar';
                const badgeClass = isGrammar
                    ? 'bg-blue-100 text-blue-600'
                    : 'bg-orange-100 text-orange-600';

                const rowHtml = `
      <tr class="border-b border-gray-100 hover:bg-vio-pale transition-colors">
        <td class="px-3 py-2 italic">${item.quizSentence}</td>
        <td class="px-3 py-2 text-green-600 font-bold">${item.word}</td>
        <td class="px-3 py-2 text-red-500">${item.userAnswer}</td>
        <td class="px-3 py-2">
            <span class="px-2 py-0.5 ${badgeClass} rounded-full text-xs">${item.quizType}</span>
        </td>
        <td class="px-3 py-2">${Number(item.responseTime).toFixed(1)}s</td>
      </tr>
    `;
                quizLogTable.insertAdjacentHTML('beforeend', rowHtml);
            });
        }
    });
}


function getAiBoard() {
    $.ajax({
        type: "GET",
        url: `/my-page/ai/${username}`,
        contentType: false,
        processData: false,
        success: function (response) {
            // 어휘 정답률
            if (response.vocabularyCorrectRate !== null) {
                var vocabRate = Math.floor(response.vocabularyCorrectRate);
                $('#vocab-rate').text(vocabRate + '%');
            } else {
                $('#vocab-rate').text('0%');
            }

            // 문법 정답률
            if (response.grammarCorrectRate !== null) {
                var grammarRate = Math.floor(response.grammarCorrectRate);
                $('#grammar-rate').text(grammarRate + '%');
            } else {
                $('#grammar-rate').text('0%');
            }

            // 평균 응답 시간
            if (response.avgResponseTime !== null) {
                var avgTime = response.avgResponseTime.toFixed(1);
                $('#avg-time').text(avgTime + 's');
            } else {
                $('#avg-time').text('0.0s');
            }
        },
        error: function (xhr, status, error) {
            console.error("통계 데이터를 가져오는 중 오류가 발생했습니다: ", error);
        }
    });
}

function fetchAiFeedbackData(targetUrl) {
    const $feedbackEl = $('#ai-feedback');
    const $patternEl = $('#error-pattern');
    const $spinner = $('#loading-spinner');

    // 로딩 상태 UI 처리
    $feedbackEl.css('opacity', 0).hide(); // 한 줄로 묶기 가능
    $spinner.removeClass('hidden').addClass('flex');

    $.ajax({
        type: "GET",
        url: targetUrl, // 매개변수로 받은 URL 적용
        success: function (response) {
            setTimeout(() => {
                $patternEl.text(response.errorPattern);
                $feedbackEl.html(response.aiFeedback);

                $spinner.removeClass('flex').addClass('hidden');
                $feedbackEl.show().css('opacity', 1);
            }, 200);
        },
        error: function (xhr, status, error) {
            setTimeout(() => {
                $patternEl.text("통신 오류");
                $feedbackEl.html("AI 분석을 불러오지 못했습니다. 다시 시도해주세요.");
                $spinner.removeClass('flex').addClass('hidden'); // 기존 코드의 .hide() 대신 일관되게 클래스 제어
                $feedbackEl.show().css('opacity', 1);
            }, 200);
        }
    });
}

// ai 분석 새로고침
function refreshFeedback() {
    fetchAiFeedbackData(`/my-page/ai/${username}/refresh`);
}
// 기존 db에서 ai 피드백 가져오기
function aiFeedback() {
    fetchAiFeedbackData(`/my-page/ai/${username}/feedback`);
}


// 페이지 로드
$(document).ready(function () {
    get7dayTime();
    levelPie();
    getRandomQuiz();
    getRecentWrongQuiz();
    getAiBoard();
    aiFeedback();
    $("#blank-nickname").text("🏠 " + username + " 🏠");
    // document.getElementById('ai-feedback').innerHTML = feedbackList[0];
});

// 학습/풀이 시간 바 차트
function get7dayTime() {
    $.ajax({
        type: "GET",
        url: `/my-page/time/${username}`,
        contentType: false,
        processData: false,
        success: function (response) {
            const today = new Date();
            const weekDays = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
            const weekDaysKo = ['일', '월', '화', '수', '목', '금', '토'];
            const startIndex = today.getDay();
            let studyData = Array(7).fill(0);
            let quizData = Array(7).fill(0);

            for (let i = 0; i < response.length; i++) {
                const x = new Date(response[i].date).getDay();
                studyData.splice(x, 1, (response[i].study_time / 60).toFixed(1));
                quizData.splice(x, 1, (response[i].quiz_time / 60).toFixed(1));
            }

            const orderedDays = [...weekDaysKo.slice(startIndex + 1), ...weekDaysKo.slice(0, startIndex + 1)];
            studyData = [...studyData.slice(startIndex + 1), ...studyData.slice(0, startIndex + 1)];
            quizData = [...quizData.slice(startIndex + 1), ...quizData.slice(0, startIndex + 1)];

            new Chart(document.getElementById('timeChart'), {
                type: 'bar',
                data: {
                    labels: orderedDays,
                    datasets: [
                        {label: '학습 시간 (분)', data: studyData, backgroundColor: 'rgba(90,75,129,0.7)', borderRadius: 4},
                        {label: '풀이 시간 (분)', data: quizData, backgroundColor: 'rgba(196,181,232,0.8)', borderRadius: 4},
                    ]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,  // ✅ 높이 고정 핵심
                    plugins: {legend: {position: 'bottom', labels: {font: {size: 11}}}},
                    scales: {
                        y: {
                            beginAtZero: true,
                            title: {display: true, text: '시간 (분)'},
                            grid: {color: 'rgba(0,0,0,0.04)'}
                        },
                        x: {grid: {display: false}}
                    }
                }
            });
        },
        error: function () {
            // API 없을 때 예시 데이터
            new Chart(document.getElementById('timeChart'), {
                type: 'bar',
                data: {
                    labels: ['월', '화', '수', '목', '금', '토', '일'],
                    datasets: [
                        {
                            label: '학습 시간 (분)',
                            data: [30, 45, 20, 60, 35, 50, 40],
                            backgroundColor: 'rgba(90,75,129,0.7)',
                            borderRadius: 4
                        },
                        {
                            label: '풀이 시간 (분)',
                            data: [15, 20, 10, 30, 15, 25, 20],
                            backgroundColor: 'rgba(196,181,232,0.8)',
                            borderRadius: 4
                        },
                    ]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,  // ✅ 높이 고정 핵심
                    plugins: {legend: {position: 'bottom', labels: {font: {size: 11}}}},
                    scales: {
                        y: {beginAtZero: true, grid: {color: 'rgba(0,0,0,0.04)'}},
                        x: {grid: {display: false}}
                    }
                }
            });
        }
    });
}

// Level별 학습 비율 가로 bar 차트 (이미지와 동일)
function levelPie() {
    $.ajax({
        type: "GET",
        url: `/my-page/level/${username}`,
        contentType: false,
        processData: false,
        success: function (response) {
            const total = response.reduce((sum, l) => sum + l.cnt, 0);
            const sorted = response.sort((a, b) => a.level - b.level);
            const percentages = sorted.map(l => ((l.cnt / total) * 100).toFixed(1));

            new Chart(document.getElementById('additionalChart'), {
                type: 'bar',
                data: {
                    labels: sorted.map(l => `Level ${l.level}`),
                    datasets: [{
                        data: percentages,
                        backgroundColor: ['#C4B5E8', '#9B89D8', '#7A65C8', '#5A4B81', '#3d3060'],
                        borderWidth: 0,
                        borderRadius: 4
                    }]
                },
                options: {
                    indexAxis: 'y',              // ✅ 가로 막대 (이미지와 동일)
                    responsive: true,
                    maintainAspectRatio: false,  // ✅ 높이 고정 핵심
                    plugins: {legend: {display: false}},
                    scales: {
                        x: {beginAtZero: true, grid: {color: 'rgba(0,0,0,0.04)'}},
                        y: {grid: {display: false}}
                    }
                }
            });
        },
        error: function () {
            // API 없을 때 예시 데이터
            new Chart(document.getElementById('additionalChart'), {
                type: 'bar',
                data: {
                    labels: ['Level 1', 'Level 2', 'Level 3', 'Level 4', 'Level 5'],
                    datasets: [{
                        data: [35, 27, 20, 12, 6],
                        backgroundColor: ['#C4B5E8', '#9B89D8', '#7A65C8', '#5A4B81', '#3d3060'],
                        borderWidth: 0,
                        borderRadius: 4
                    }]
                },
                options: {
                    indexAxis: 'y',
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {legend: {display: false}},
                    scales: {
                        x: {beginAtZero: true, grid: {color: 'rgba(0,0,0,0.04)'}},
                        y: {grid: {display: false}}
                    }
                }
            });
        }
    });
}

// Level 이미지 팝업 (SweetAlert2)
function levelPieIMG() {
    const url = `http://127.0.0.1:5000/my-page/level/image/${username}`;
    $.ajax({
        type: "GET",
        url: url,
        xhrFields: {responseType: 'blob'},
        success: function (response) {
            const imgURL = URL.createObjectURL(new Blob([response], {type: 'image/png'}));
            Swal.fire({
                title: 'Level별 학습 비율',
                imageUrl: imgURL,
                imageAlt: 'Level Bar Chart',
                background: '#F4EBFF',
                confirmButtonColor: '#5A4B81',
                confirmButtonText: '닫기',
                width: 680,
            });
        },
        error: function () {
            Swal.fire({
                title: 'Level별 학습 비율',
                html: '<canvas id="sweetBar" width="300" height="300"></canvas>',
                background: '#F4EBFF',
                confirmButtonColor: '#5A4B81',
                confirmButtonText: '닫기',
                didOpen: () => {
                    new Chart(document.getElementById('sweetBar'), {
                        type: 'bar',
                        data: {
                            labels: ['Level 1', 'Level 2', 'Level 3', 'Level 4', 'Level 5'],
                            datasets: [{
                                data: [35, 27, 20, 12, 6],
                                backgroundColor: ['#C4B5E8', '#9B89D8', '#7A65C8', '#5A4B81', '#3d3060'],
                                borderWidth: 0
                            }]
                        },
                        options: {
                            indexAxis: 'y',
                            responsive: false,
                            plugins: {legend: {display: false}},
                            scales: {
                                x: {beginAtZero: true},
                                y: {grid: {display: false}}
                            }
                        }
                    });
                }
            });
        }
    });
}

// // AI 피드백 새로 고침
// const feedbackList = [
//   '사용자는 <strong>어휘(42%) 영역</strong>에서 취약점이 관찰됩니다. 품사 혼동 패턴(명사↔동사)이 반복되고 있습니다.<br><em style="color:#5A4B81">→ 품사 구분 집중 학습을 권장합니다.</em>',
//   '응답 시간이 <strong>6초 이상</strong>인 오답이 다수 확인됩니다. 해당 어휘에 대한 이해도가 낮음을 시사합니다.<br><em style="color:#5A4B81">→ 기초 어휘 반복 학습이 필요합니다.</em>',
//   '문법 정답률(81%)은 양호하나, 어휘 정답률(42%)과의 <strong>39%p 격차</strong>가 큽니다.<br><em style="color:#5A4B81">→ 어휘 학습에 더 많은 시간 배분을 권장합니다.</em>',
// ];
// let feedbackIdx = 0;
//
// function refreshFeedback() {
//   feedbackIdx = (feedbackIdx + 1) % feedbackList.length;
//   const el = document.getElementById('ai-feedback');
//   el.style.opacity = 0;
//   setTimeout(() => {
//     el.innerHTML = feedbackList[feedbackIdx];
//     el.style.opacity = 1;
//   }, 200);
// }










