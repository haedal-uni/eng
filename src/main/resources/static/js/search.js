const searchInput = document.getElementById("search-input");
const suggestions = document.getElementById('suggestions');

searchInput.addEventListener("input", () => {
    const query = searchInput.value;
    if (query.length > 0) {
        fetch(`/autocomplete?query=${query}`, {
            method: "GET",
            headers: {
                "Content-Type": "application/json"
            }
        })
            .then(response => response.json())
            .then(data => {
                if (Object.entries(data[0]).length>0 || Object.entries(data[1]).length>0) {
                    suggestions.style.display = 'block';
                    suggestions.innerHTML = ''; // 기존 내용 초기화

                    // startwith
                    let startData = data[0];
                    Object.entries(startData).forEach(([key, values]) => {
                        const li = document.createElement('li');
                        li.innerHTML = `${highlightMatching(key, query)} : ${values.join(', ')}`;
                        suggestions.appendChild(li);
                    });

                    // endwith
                    let endData = data[1];
                    Object.entries(endData).forEach(([key, values]) => {
                        const li = document.createElement('li');
                        li.innerHTML = `${highlightMatching(key, query)} : ${values.join(', ')}`;
                        suggestions.appendChild(li);
                    });
                } else {
                    suggestions.style.display = 'none'; // 결과 없음
                }
            })
            .catch(error => {
                console.error('Error : ', error);
                suggestions.style.display = 'none';
            });
    } else {
        suggestions.style.display = 'none';
        suggestions.innerHTML = ''; // 초기화
    }
});

function highlightMatching(word, query) {
    const regex = new RegExp(`(${query})`, 'gi'); // 입력된 문자열과 일치하는 부분
    return word.replace(regex, '<span class="highlight">$1</span>'); // 강조 표시
}