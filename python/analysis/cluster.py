import pandas as pd
data = pd.read_csv('../../../kaggle/StudentPerformanceFactors.csv')
data = data[data["Exam_Score"] <= 100]

from scipy.cluster.hierarchy import dendrogram, linkage
from matplotlib import pyplot as plt
XX = data[['Hours_Studied', 'Exam_Score']]

# 군집 분석 수행 (연결 방법: 'ward')
Z = linkage(XX, method='ward')

# 덴드로그램 그리기
plt.figure(figsize=(10, 7))
dendrogram(Z)
plt.show()