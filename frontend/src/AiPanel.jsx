import { useState } from 'react';
import axios from 'axios';
import './AiPanel.css';

const AI_API_URL = 'http://192.168.29.50:8081/api/ai';

function AiPanel({ todos }) {
  const [activeTab, setActiveTab] = useState('summary');
  const [loading, setLoading] = useState(false);
  const [summary, setSummary] = useState('');
  const [question, setQuestion] = useState('');
  const [answer, setAnswer] = useState('');
  const [categories, setCategories] = useState([]);
  const [priorities, setPriorities] = useState([]);
  const [recommendations, setRecommendations] = useState([]);

  const getSummary = async () => {
    try {
      setLoading(true);
      const response = await axios.get(`${AI_API_URL}/summarize`);
      setSummary(response.data.answer);
    } catch (error) {
      setSummary('Failed to get summary: ' + error.message);
    } finally {
      setLoading(false);
    }
  };

  const askQuestion = async (e) => {
    e.preventDefault();
    if (!question.trim()) return;

    try {
      setLoading(true);
      const response = await axios.post(`${AI_API_URL}/question`, { question });
      setAnswer(response.data.answer);
    } catch (error) {
      setAnswer('Failed to get answer: ' + error.message);
    } finally {
      setLoading(false);
    }
  };

  const categorize = async () => {
    try {
      setLoading(true);
      const response = await axios.get(`${AI_API_URL}/categorize`);
      setCategories(response.data);
    } catch (error) {
      console.error('Failed to categorize:', error);
    } finally {
      setLoading(false);
    }
  };

  const prioritize = async () => {
    try {
      setLoading(true);
      const response = await axios.get(`${AI_API_URL}/prioritize`);
      setPriorities(response.data);
    } catch (error) {
      console.error('Failed to prioritize:', error);
    } finally {
      setLoading(false);
    }
  };

  const getRecommendations = async () => {
    try {
      setLoading(true);
      const response = await axios.get(`${AI_API_URL}/recommend`);
      setRecommendations(response.data);
    } catch (error) {
      console.error('Failed to get recommendations:', error);
    } finally {
      setLoading(false);
    }
  };

  const getTaskTitle = (taskId) => {
    const task = todos.find(t => t.id === taskId);
    return task ? task.title : 'Unknown Task';
  };

  return (
    <div className="ai-panel">
      <h2>🤖 AI Assistant</h2>

      <div className="ai-tabs">
        <button
          className={activeTab === 'summary' ? 'active' : ''}
          onClick={() => setActiveTab('summary')}
        >
          Summary
        </button>
        <button
          className={activeTab === 'qna' ? 'active' : ''}
          onClick={() => setActiveTab('qna')}
        >
          Q&A
        </button>
        <button
          className={activeTab === 'categorize' ? 'active' : ''}
          onClick={() => setActiveTab('categorize')}
        >
          Categorize
        </button>
        <button
          className={activeTab === 'prioritize' ? 'active' : ''}
          onClick={() => setActiveTab('prioritize')}
        >
          Prioritize
        </button>
        <button
          className={activeTab === 'recommend' ? 'active' : ''}
          onClick={() => setActiveTab('recommend')}
        >
          Recommend
        </button>
      </div>

      <div className="ai-content">
        {activeTab === 'summary' && (
          <div>
            <button onClick={getSummary} disabled={loading} className="btn btn-primary">
              {loading ? 'Generating...' : 'Generate Summary'}
            </button>
            {summary && <div className="ai-result">{summary}</div>}
          </div>
        )}

        {activeTab === 'qna' && (
          <div>
            <form onSubmit={askQuestion}>
              <input
                type="text"
                placeholder="Ask anything about your tasks..."
                value={question}
                onChange={(e) => setQuestion(e.target.value)}
                className="input"
              />
              <button type="submit" disabled={loading} className="btn btn-primary">
                {loading ? 'Thinking...' : 'Ask'}
              </button>
            </form>
            {answer && <div className="ai-result">{answer}</div>}
          </div>
        )}

        {activeTab === 'categorize' && (
          <div>
            <button onClick={categorize} disabled={loading} className="btn btn-primary">
              {loading ? 'Categorizing...' : 'Categorize All Tasks'}
            </button>
            {categories.length > 0 && (
              <div className="analysis-list">
                {categories.map((item) => (
                  <div key={item.taskId} className="analysis-item">
                    <strong>{getTaskTitle(item.taskId)}</strong>
                    <span className="badge">{item.category}</span>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}

        {activeTab === 'prioritize' && (
          <div>
            <button onClick={prioritize} disabled={loading} className="btn btn-primary">
              {loading ? 'Analyzing...' : 'Prioritize All Tasks'}
            </button>
            {priorities.length > 0 && (
              <div className="analysis-list">
                {priorities.map((item) => (
                  <div key={item.taskId} className="analysis-item">
                    <strong>{getTaskTitle(item.taskId)}</strong>
                    <span className={`badge priority-${item.priority.toLowerCase()}`}>
                      {item.priority}
                    </span>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}

        {activeTab === 'recommend' && (
          <div>
            <button onClick={getRecommendations} disabled={loading} className="btn btn-primary">
              {loading ? 'Generating...' : 'Get Recommendations'}
            </button>
            {recommendations.length > 0 && (
              <div className="analysis-list">
                {recommendations.map((item) => (
                  <div key={item.taskId} className="analysis-item">
                    <strong>{getTaskTitle(item.taskId)}</strong>
                    <p>{item.recommendation}</p>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
}

export default AiPanel;