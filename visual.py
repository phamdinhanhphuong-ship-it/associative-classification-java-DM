import matplotlib.pyplot as plt
import pandas as pd

def visualize_class_distribution(data, class_column):
    """
    Visualizes the histogram of class distribution in a dataset.

    Parameters:
        data (pd.DataFrame): The dataset containing the class labels.
        class_column (str): The name of the column containing class labels.
    """

    if class_column not in data.columns:
        raise ValueError(f"Class column '{class_column}' not found in the dataset.")

    class_counts = data[class_column].value_counts()

    plt.figure(figsize=(8, 6))
    class_counts.plot(kind='bar')
    plt.title('Class Distribution')
    plt.xlabel('Class')
    plt.ylabel('Frequency')
    plt.xticks(rotation=45)  # Rotate x-axis labels if needed
    plt.show()

if __name__ == '__main__':
    dataset_path = "mushrooms.txt"  # Replace with your dataset path
    class_attribute_name = "poisonous"  # Replace with your class attribute name
    data = pd.read_csv(dataset_path, header=None)

    for i in range(data.shape[1] - 1):
        data[i] = data[i].astype(str) + str(i)
    data.columns = [f'feature_{i}' for i in range(data.shape[1] - 1)] + ['target']
    # Assuming 'class' is the target column in the iris dataset
    # data.columns = ['sepal_length', 'sepal_width', 'petal_length', 'petal_width', 'target']

    visualize_class_distribution(data, 'target')

