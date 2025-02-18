import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns

data = pd.read_csv('../../../kaggle/StudentPerformanceFactors.csv')
data = data[data["Exam_Score"] <= 100]

correlation = data['Hours_Studied'].corr(data['Exam_Score'])
print(f"공부 시간과 시험 점수 간의 Pearson 상관계수: {correlation}")

sns.lmplot(x='Hours_Studied', y='Exam_Score', data=data)
plt.show()

data['study_time'] = pd.cut(data['Hours_Studied'], bins=[0, 15, 30, 45], labels=['적음', '보통', '많음'])
data['exam'] = pd.cut(data['Exam_Score'], bins=[50, 65, 75, 100], labels=['낮음', '중간', '높음'])
observed = pd.crosstab(data['study_time'], data['exam'])

# 적합도
expected = chi2_contingency(observed)[3]
cell = (expected < 5).sum()
percentage = (cell / expected.size) * 100
if percentage >= 25:
    print("카이제곱 검정을 적용x")

# 독립성
chi2, p, dof, expected = chi2_contingency(observed)
print(f"카이제곱 통계량: {chi2}, p-value: {p}")
print(f"기대 치: \n{expected.round(1)}, \n자유도: {dof}")

# 기여도
residuals = (observed - expected) / np.sqrt(expected)
print(residuals)