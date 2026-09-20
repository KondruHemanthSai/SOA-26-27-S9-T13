import { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import TopNav from '../../components/layout/TopNav';
import StudentSidebar from '../../components/layout/StudentSidebar';
import { aiApi } from '../../services/api';
import type { ChatMessage } from '../../services/api';

const QUICK_PROMPTS = [
  'What courses am I registered for?',
  'When is my next class?',
  'What is my admission status?',
  'Recommend courses for me this semester',
  'Do I have any timetable conflicts?',
];

export default function StudentAIAssistant() {
  const navigate = useNavigate();
  const [messages, setMessages] = useState<ChatMessage[]>([
    {
      role: 'assistant',
      content:
        'Hello! I am your Student Central AI Academic Assistant. I can answer questions about your registered courses, upcoming classes, admission status, and recommended courses based on your official campus records.',
      suggestedActions: ['Explore Courses', 'View Timetable', 'View Admission'],
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    },
  ]);
  const [inputMessage, setInputMessage] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [remainingLimit, setRemainingLimit] = useState<number | null>(null);
  const [aiAvailable, setAiAvailable] = useState<boolean>(true);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    aiApi.getHealth()
      .then((res) => setAiAvailable(res.available || res.status === 'UP'))
      .catch(() => setAiAvailable(true)); // Fallback provider is always active
  }, []);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages, isLoading]);

  const handleSendMessage = async (textToSend?: string) => {
    const text = (textToSend || inputMessage).trim();
    if (!text || isLoading) return;

    const userMsg: ChatMessage = {
      role: 'user',
      content: text,
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    };

    setMessages((prev) => [...prev, userMsg]);
    if (!textToSend) setInputMessage('');
    setIsLoading(true);

    try {
      const response = await aiApi.chat(text);
      const assistantMsg: ChatMessage = {
        role: 'assistant',
        content: response.message,
        suggestedActions: response.suggestedActions,
        toolsUsed: response.toolsUsed,
        timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      };
      setMessages((prev) => [...prev, assistantMsg]);
      if (response.remainingRateLimit !== undefined) {
        setRemainingLimit(response.remainingRateLimit);
      }
    } catch {
      const fallbackMsg: ChatMessage = {
        role: 'assistant',
        content:
          'AI assistance is temporarily operating in resilience mode. You can continue accessing all official course and timetable data normally from your dashboard.',
        suggestedActions: ['Explore Courses', 'View Timetable'],
        timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      };
      setMessages((prev) => [...prev, fallbackMsg]);
    } finally {
      setIsLoading(false);
    }
  };

  const handleActionClick = (action: string) => {
    if (action.includes('Course') || action.includes('Catalog')) {
      navigate('/student/courses');
    } else if (action.includes('Timetable') || action.includes('Schedule')) {
      navigate('/student/timetable');
    } else if (action.includes('Admission')) {
      navigate('/student/admission');
    } else if (action.includes('Notification')) {
      navigate('/student/notifications');
    } else if (action.includes('Registration') || action.includes('Enroll')) {
      navigate('/student/registrations');
    }
  };

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col">
      <TopNav />
      <div className="flex-1 flex overflow-hidden">
        <StudentSidebar />
        <main className="flex-1 flex flex-col h-[calc(100vh-64px)] overflow-hidden">
          {/* Header */}
          <div className="bg-white border-b border-slate-200 px-8 py-4 flex items-center justify-between shrink-0">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-indigo-600 to-indigo-500 flex items-center justify-center text-white shadow-md shadow-indigo-100 font-bold">
                AI
              </div>
              <div>
                <h1 className="text-xl font-bold text-slate-900">Student AI Assistant</h1>
                <p className="text-xs text-slate-500">
                  Context-aware intelligence grounded in real Student Central records
                </p>
              </div>
            </div>
            <div className="flex items-center gap-3">
              {remainingLimit !== null && (
                <span className="text-xs px-2.5 py-1 rounded-full bg-slate-100 text-slate-600 font-medium">
                  {remainingLimit}/20 requests remaining this hour
                </span>
              )}
              <span className={`inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-semibold ${
                aiAvailable ? 'bg-emerald-50 text-emerald-700 border border-emerald-200' : 'bg-amber-50 text-amber-700'
              }`}>
                <span className={`w-2 h-2 rounded-full ${aiAvailable ? 'bg-emerald-500 animate-pulse' : 'bg-amber-500'}`} />
                {aiAvailable ? 'AI Grounded Online' : 'Resilience Mode'}
              </span>
            </div>
          </div>

          {/* Quick Prompt Chips */}
          <div className="bg-slate-100/60 border-b border-slate-200/80 px-8 py-2.5 flex items-center gap-2 overflow-x-auto shrink-0">
            <span className="text-xs font-semibold text-slate-500 shrink-0">Quick Queries:</span>
            {QUICK_PROMPTS.map((prompt, i) => (
              <button
                key={i}
                onClick={() => handleSendMessage(prompt)}
                disabled={isLoading}
                className="text-xs bg-white hover:bg-indigo-50 hover:text-indigo-600 hover:border-indigo-200 text-slate-700 px-3 py-1 rounded-full border border-slate-200 shadow-sm transition whitespace-nowrap shrink-0 disabled:opacity-50"
              >
                {prompt}
              </button>
            ))}
          </div>

          {/* Chat Messages */}
          <div className="flex-1 overflow-y-auto px-8 py-6 space-y-6">
            {messages.map((msg, idx) => (
              <div
                key={idx}
                className={`flex gap-3 max-w-3xl ${
                  msg.role === 'user' ? 'ml-auto flex-row-reverse' : 'mr-auto'
                }`}
              >
                {/* Avatar */}
                <div
                  className={`w-8 h-8 rounded-full flex items-center justify-center shrink-0 text-xs font-bold ${
                    msg.role === 'user'
                      ? 'bg-indigo-600 text-white shadow'
                      : 'bg-slate-800 text-white shadow'
                  }`}
                >
                  {msg.role === 'user' ? 'ME' : 'AI'}
                </div>

                {/* Message Bubble */}
                <div className="space-y-2">
                  <div
                    className={`rounded-2xl px-5 py-3.5 text-sm shadow-sm ${
                      msg.role === 'user'
                        ? 'bg-indigo-600 text-white rounded-tr-none'
                        : 'bg-white border border-slate-200 text-slate-800 rounded-tl-none leading-relaxed'
                    }`}
                  >
                    <p className="whitespace-pre-wrap">{msg.content}</p>

                    {/* Tools Used Badge */}
                    {msg.toolsUsed && msg.toolsUsed.length > 0 && (
                      <div className="mt-3 pt-2 border-t border-slate-100 flex flex-wrap items-center gap-1.5 text-[11px] text-slate-400">
                        <span>Authoritative tools:</span>
                        {msg.toolsUsed.map((tool, tIdx) => (
                          <span
                            key={tIdx}
                            className="bg-slate-100 text-slate-600 font-mono px-2 py-0.5 rounded text-[10px]"
                          >
                            {tool}()
                          </span>
                        ))}
                      </div>
                    )}
                  </div>

                  {/* Suggested Action Buttons */}
                  {msg.suggestedActions && msg.suggestedActions.length > 0 && (
                    <div className="flex flex-wrap gap-2 pt-1">
                      {msg.suggestedActions.map((action, aIdx) => (
                        <button
                          key={aIdx}
                          onClick={() => handleActionClick(action)}
                          className="inline-flex items-center gap-1 px-3 py-1 rounded-lg text-xs font-medium bg-indigo-50 text-indigo-700 hover:bg-indigo-100 border border-indigo-200 transition shadow-xs"
                        >
                          <span>{action}</span>
                          <span>→</span>
                        </button>
                      ))}
                    </div>
                  )}

                  {msg.timestamp && (
                    <p
                      className={`text-[10px] text-slate-400 ${
                        msg.role === 'user' ? 'text-right' : 'text-left'
                      }`}
                    >
                      {msg.timestamp}
                    </p>
                  )}
                </div>
              </div>
            ))}

            {isLoading && (
              <div className="flex gap-3 mr-auto max-w-2xl">
                <div className="w-8 h-8 rounded-full bg-slate-800 text-white flex items-center justify-center shrink-0 text-xs font-bold">
                  AI
                </div>
                <div className="bg-white border border-slate-200 rounded-2xl rounded-tl-none px-5 py-3.5 shadow-sm">
                  <div className="flex items-center gap-2 text-sm text-slate-500">
                    <span className="w-2 h-2 rounded-full bg-indigo-600 animate-bounce" />
                    <span className="w-2 h-2 rounded-full bg-indigo-600 animate-bounce [animation-delay:0.2s]" />
                    <span className="w-2 h-2 rounded-full bg-indigo-600 animate-bounce [animation-delay:0.4s]" />
                    <span className="text-xs font-medium ml-1">Consulting Student Central microservices...</span>
                  </div>
                </div>
              </div>
            )}
            <div ref={messagesEndRef} />
          </div>

          {/* Chat Input */}
          <div className="p-4 bg-white border-t border-slate-200 shrink-0">
            <form
              onSubmit={(e) => {
                e.preventDefault();
                handleSendMessage();
              }}
              className="max-w-4xl mx-auto flex gap-2"
            >
              <input
                type="text"
                value={inputMessage}
                onChange={(e) => setInputMessage(e.target.value)}
                placeholder="Ask about your registrations, timetable, courses, or admission status..."
                disabled={isLoading}
                className="flex-1 px-4 py-3 bg-slate-50 border border-slate-300 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent transition"
              />
              <button
                type="submit"
                disabled={!inputMessage.trim() || isLoading}
                className="px-6 py-3 bg-indigo-600 text-white font-medium text-sm rounded-xl hover:bg-indigo-700 disabled:opacity-50 transition shadow-sm"
              >
                Send
              </button>
            </form>
          </div>
        </main>
      </div>
    </div>
  );
}
