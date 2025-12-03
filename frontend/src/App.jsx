import { useState, useEffect } from 'react';
import axios from 'axios';
import './App.css';

const API_URL = 'http://localhost:8081/api/todos';

function App() {
  const [todos, setTodos] = useState([]);
  const [newTodo, setNewTodo] = useState({ title: '', description: '' });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchTodos();
  }, []);

  const fetchTodos = async () => {
    try {
      setLoading(true);
      setError('');
      const response = await axios.get(API_URL);
      setTodos(response.data);
    } catch (err) {
      setError('Failed to fetch todos');
    } finally {
      setLoading(false);
    }
  };

  const addTodo = async (e) => {
    e.preventDefault();
    if (!newTodo.title.trim()) return;

    try {
      const response = await axios.post(API_URL, {
        ...newTodo,
        completed: false,
      });
      setTodos([response.data, ...todos]);
      setNewTodo({ title: '', description: '' });
    } catch {
      setError('Failed to add todo');
    }
  };

  const toggleTodo = async (id, completed) => {
    try {
      const todoToUpdate = todos.find(t => t.id === id);
      const response = await axios.put(`${API_URL}/${id}`, {
        ...todoToUpdate,
        completed: !completed,
      });
      setTodos(todos.map(t => (t.id === id ? response.data : t)));
    } catch {
      setError('Failed to update todo');
    }
  };

  const deleteTodo = async (id) => {
    try {
      await axios.delete(`${API_URL}/${id}`);
      setTodos(todos.filter(t => t.id !== id));
    } catch {
      setError('Failed to delete todo');
    }
  };

  return (
    <div className="container">
      <h1>📝 Todo App</h1>
      <p>React + Spring Boot + PostgreSQL</p>

      {error && <p className="error">{error}</p>}

      <form onSubmit={addTodo} className="todo-form">
        <input
          type="text"
          placeholder="Todo title..."
          value={newTodo.title}
          onChange={(e) => setNewTodo({ ...newTodo, title: e.target.value })}
          className="input"
        />
        <input
          type="text"
          placeholder="Description (optional)..."
          value={newTodo.description}
          onChange={(e) => setNewTodo({ ...newTodo, description: e.target.value })}
          className="input"
        />
        <button type="submit" className="btn btn-primary">
          Add Todo
        </button>
      </form>

      {loading ? (
        <p>Loading...</p>
      ) : todos.length === 0 ? (
        <p>No todos yet. Add one above!</p>
      ) : (
        <ul className="todo-list">
          {todos.map(todo => (
            <li
              key={todo.id}
              className={`todo-item ${todo.completed ? 'completed' : ''}`}
            >
              <div className="todo-content">
                <input
                  type="checkbox"
                  checked={todo.completed}
                  onChange={() => toggleTodo(todo.id, todo.completed)}
                  className="checkbox"
                />

                <div className="todo-text">
                  <h3>{todo.title}</h3>

                  {todo.description && (
                    <p>{todo.description}</p>
                  )}

                  <small>
                    Created: {new Date(todo.createdAt).toLocaleString()}
                  </small>
                </div>
              </div>

              <button
                onClick={() => deleteTodo(todo.id)}
                className="btn btn-danger"
              >
                Delete
              </button>
            </li>
          ))}
        </ul>

      )}
    </div>
  );
}

export default App;
