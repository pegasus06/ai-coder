document.addEventListener('DOMContentLoaded', function() {
    const form = document.getElementById('task-form');
    const input = document.getElementById('task-input-field');
    const taskList = document.getElementById('tasks');

    // 加载保存的任务
    loadTasks();

    form.addEventListener('submit', function(e) {
        e.preventDefault();
        const taskText = input.value.trim();
        if (taskText === '') return;

        addTask(taskText);
        saveTasks();
        input.value = '';
    });

    function addTask(text) {
        const li = document.createElement('li');
        li.className = 'task-item';
        li.innerHTML = `<span class="task-text">${text}</span>
                        <button class="delete-btn">删除</button>`;
        
        li.querySelector('.delete-btn').addEventListener('click', function() {
            li.remove();
            saveTasks();
        });

        taskList.appendChild(li);
    }

    function saveTasks() {
        const tasks = [];
        document.querySelectorAll('.task-item').forEach(function(item) {
            tasks.push(item.querySelector('.task-text').textContent);
        });
        localStorage.setItem('tasks', JSON.stringify(tasks));
    }

    function loadTasks() {
        const storedTasks = localStorage.getItem('tasks');
        if (storedTasks) {
            const tasks = JSON.parse(storedTasks);
            tasks.forEach(function(task) {
                addTask(task);
            });
        }
    }
});