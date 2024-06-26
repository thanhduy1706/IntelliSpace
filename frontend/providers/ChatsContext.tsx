'use client';
import { createContext, useState } from 'react';
import { sendPrompt } from '@/lib/apiCall';

type ChatContextProps = {
  sendPrompt: (prompt: string) => Promise<void>;
  setRecentPrompt: React.Dispatch<React.SetStateAction<string>>;
  setPrompt: React.Dispatch<React.SetStateAction<string>>;
  startNewChat: () => void;
  recentPrompt: string;
  prompt: string;
  isPending: boolean;
  isGenerating: boolean;
  output: string;
  showResult: boolean;
};

export const ChatContext = createContext<ChatContextProps>({
  sendPrompt: async () => {},
  setRecentPrompt: () => {},
  setPrompt: () => {},
  startNewChat: () => {},
  recentPrompt: '',
  prompt: '',
  isPending: false,
  isGenerating: false,
  output: '',
  showResult: false,
});

export const ChatContextProvider = ({ children }: React.PropsWithChildren) => {
  const [recentPrompt, setRecentPrompt] = useState<string>('');
  const [prompt, setPrompt] = useState<string>('');
  const [isPending, setIsPending] = useState<boolean>(false);
  const [isGenerating, setIsGenerating] = useState<boolean>(false);
  const [output, setOutput] = useState<string>('');
  const [showResult, setShowResult] = useState<boolean>(false);

  const handleNewChat = () => {
    setRecentPrompt('');
    setOutput('');
    setShowResult(false);
  };

  const handleSendPrompt = async (prompt: string) => {
    setIsGenerating(true);
    setIsPending(true);
    setRecentPrompt(prompt);
    setShowResult(true);

    // console.log(prompt)
    const promptResponse = await sendPrompt(prompt);
    // console.log(promptResponse)
    setOutput(promptResponse);
    setIsPending(false);
    setPrompt('');
    setIsGenerating(false);
  };

  // const simulateTypingEffect = (idx: number, nextWord: string): Promise<void> =>
  //   new Promise((resolve) =>
  //     setTimeout(() => {
  //       setOutput((prev) => prev + nextWord);
  //       resolve();
  //     }, 40 * idx)
  //   );

  return (
    <ChatContext.Provider
      value={{
        sendPrompt: handleSendPrompt,
        setRecentPrompt,
        setPrompt,
        startNewChat: handleNewChat,
        recentPrompt,
        prompt,
        isPending,
        isGenerating,
        output,
        showResult,
      }}
    >
      {children}
    </ChatContext.Provider>
  );
};
