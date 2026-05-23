const searchInput = document.getElementById("search-input");
const suggestions = document.getElementById('suggestions');

searchInput.addEventListener("input", () => {
  const query = searchInput.value;
  if (query.length > 0) {
    fetch(`/autocomplete?query=${query}`, {
      method: "GET",
      headers: { "Content-Type": "application/json" }
    })
      .then(response => response.json())
      .then(data => {
        if (Object.entries(data[0]).length > 0 || Object.entries(data[1]).length > 0) {
          suggestions.style.display = 'block';
          suggestions.innerHTML = '';

          Object.entries(data[0]).forEach(([key, values]) => {
            const li = document.createElement('li');
            li.innerHTML = `${highlightMatching(key, query)} : ${values.join(', ')}`;
            suggestions.appendChild(li);
          });

          Object.entries(data[1]).forEach(([key, values]) => {
            const li = document.createElement('li');
            li.innerHTML = `${highlightMatching(key, query)} : ${values.join(', ')}`;
            suggestions.appendChild(li);
          });
        } else {
          suggestions.style.display = 'none';
        }
      })
      .catch(() => {
        suggestions.style.display = 'none';
      });
  } else {
    suggestions.style.display = 'none';
    suggestions.innerHTML = '';
  }
});

function highlightMatching(word, query) {
  const regex = new RegExp(`(${query})`, 'gi');
  return word.replace(regex, '<span class="highlight">$1</span>');
}
