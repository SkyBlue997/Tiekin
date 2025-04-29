document.addEventListener('DOMContentLoaded', function() {
    // 选项卡切换
    const tabButtons = document.querySelectorAll('.tab-btn');
    const tabContents = document.querySelectorAll('.tab-content');
    
    tabButtons.forEach(button => {
        button.addEventListener('click', () => {
            // 移除所有活动标签
            tabButtons.forEach(btn => btn.classList.remove('active'));
            tabContents.forEach(content => content.classList.remove('active'));
            
            // 激活当前标签
            button.classList.add('active');
            const tabId = button.getAttribute('data-tab');
            document.getElementById(tabId).classList.add('active');
            
            // 如果点击的是日志标签，加载日志
            if (tabId === 'logs') {
                loadLogs();
            }
        });
    });
    
    // 加载配置信息
    loadConfig();
    
    // 配置表单提交
    const configForm = document.getElementById('config-form');
    configForm.addEventListener('submit', function(e) {
        e.preventDefault();
        saveConfig();
    });
    
    // 立即签到按钮
    const signNowBtn = document.getElementById('sign-now');
    signNowBtn.addEventListener('click', function() {
        signNow();
    });
});

// 加载配置信息
function loadConfig() {
    fetch('/api/config')
        .then(response => response.json())
        .then(data => {
            // 填充表单
            document.getElementById('bduss').value = data.bduss || '';
            document.getElementById('bark-server').value = data.barkServer || '';
            document.getElementById('bark-key').value = data.barkKey || '';
            document.getElementById('push-enabled').checked = data.pushEnabled || false;
            
            // 更新上次签到时间
            const lastSignTimeElement = document.getElementById('last-sign-time');
            if (data.lastSignTime) {
                const date = new Date(data.lastSignTime);
                lastSignTimeElement.textContent = formatDate(date);
            } else {
                lastSignTimeElement.textContent = '暂无签到记录';
            }
        })
        .catch(error => {
            console.error('加载配置失败:', error);
            showNotification('加载配置失败，请刷新页面重试');
        });
}

// 保存配置
function saveConfig() {
    const config = {
        bduss: document.getElementById('bduss').value,
        barkServer: document.getElementById('bark-server').value,
        barkKey: document.getElementById('bark-key').value,
        pushEnabled: document.getElementById('push-enabled').checked
    };
    
    fetch('/api/config', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(config)
    })
    .then(response => response.json())
    .then(data => {
        showNotification('配置保存成功');
        loadConfig(); // 重新加载配置
    })
    .catch(error => {
        console.error('保存配置失败:', error);
        showNotification('保存配置失败，请重试');
    });
}

// 立即签到
function signNow() {
    const signResult = document.getElementById('sign-result');
    signResult.innerHTML = '<div class="loading">签到中，请稍候...</div>';
    
    fetch('/api/sign', {
        method: 'POST'
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            const result = data.data;
            signResult.innerHTML = `
                <div class="log-item ${getStatusClass(result.status)}">
                    <div class="log-item-header">
                        <span class="log-date">${formatDate(new Date(result.signDate))}</span>
                        <span class="log-status ${getStatusClass(result.status)}">${result.status}</span>
                    </div>
                    <div class="log-summary">
                        总计: ${result.totalCount} | 成功: ${result.successCount} | 失败: ${result.failCount}
                    </div>
                </div>
            `;
            
            // 更新配置信息（上次签到时间）
            loadConfig();
            showNotification('签到完成');
        } else {
            signResult.innerHTML = `<div class="log-item failed">
                <div class="log-item-header">
                    <span class="log-date">${formatDate(new Date())}</span>
                    <span class="log-status failed">失败</span>
                </div>
                <div class="log-summary">${data.message}</div>
            </div>`;
            showNotification('签到失败: ' + data.message);
        }
    })
    .catch(error => {
        console.error('签到请求失败:', error);
        signResult.innerHTML = `<div class="log-item failed">
            <div class="log-item-header">
                <span class="log-date">${formatDate(new Date())}</span>
                <span class="log-status failed">错误</span>
            </div>
            <div class="log-summary">签到请求失败，请检查网络连接</div>
        </div>`;
        showNotification('签到请求失败，请检查网络连接');
    });
}

// 加载签到日志
function loadLogs() {
    const logList = document.getElementById('log-list');
    logList.innerHTML = '<div class="loading">加载中...</div>';
    
    fetch('/api/logs')
        .then(response => response.json())
        .then(logs => {
            if (logs.length === 0) {
                logList.innerHTML = '<p>暂无签到记录</p>';
                return;
            }
            
            let html = '';
            logs.forEach(log => {
                html += `
                    <div class="log-item ${getStatusClass(log.status)}" data-id="${log.id}">
                        <div class="log-item-header">
                            <span class="log-date">${formatDate(new Date(log.signDate))}</span>
                            <span class="log-status ${getStatusClass(log.status)}">${log.status}</span>
                        </div>
                        <div class="log-summary">
                            总计: ${log.totalCount} | 成功: ${log.successCount} | 失败: ${log.failCount}
                        </div>
                    </div>
                `;
            });
            
            logList.innerHTML = html;
            
            // 添加点击事件
            const logItems = document.querySelectorAll('.log-item');
            logItems.forEach(item => {
                item.addEventListener('click', function() {
                    const logId = this.getAttribute('data-id');
                    loadLogDetail(logId);
                });
            });
        })
        .catch(error => {
            console.error('加载日志失败:', error);
            logList.innerHTML = '<p>加载日志失败，请刷新重试</p>';
        });
}

// 加载日志详情
function loadLogDetail(id) {
    const logDetailCard = document.getElementById('log-detail-card');
    const logDetail = document.getElementById('log-detail');
    
    logDetail.innerHTML = '<div class="loading">加载详情中...</div>';
    logDetailCard.style.display = 'block';
    
    fetch(`/api/logs/${id}`)
        .then(response => response.json())
        .then(log => {
            logDetail.textContent = log.detail || '无详细信息';
        })
        .catch(error => {
            console.error('加载日志详情失败:', error);
            logDetail.textContent = '加载详情失败，请重试';
        });
}

// 根据状态获取对应的CSS类
function getStatusClass(status) {
    if (status === '成功') return 'success';
    if (status === '部分成功') return 'partial';
    return 'failed';
}

// 格式化日期
function formatDate(date) {
    return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
    });
}

// 显示通知
function showNotification(message) {
    // 简单的通知实现，可以替换为更好的通知库
    alert(message);
} 