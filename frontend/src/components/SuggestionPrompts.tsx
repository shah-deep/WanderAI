import React from 'react';

interface SuggestionPromptsProps {
  onSelect: (prompt: string) => void;
}

const suggestions = [
  'Plan itinerary for a 4 day trip to Vegas.',
  'Suggest places to visit in Japan',
];

const SuggestionPrompts: React.FC<SuggestionPromptsProps> = ({ onSelect }) => {
  return (
    <div className="absolute left-1/2 -translate-x-1/2 bottom-20 z-20 flex gap-1 pointer-events-none select-none">
      {suggestions.map((prompt, idx) => (
        <button
          key={idx}
          className="bg-gray-100 text-gray-700 px-2 py-1 rounded-full shadow-sm border border-gray-200 hover:bg-gray-200 transition pointer-events-auto select-auto text-xs font-medium"
          onClick={() => onSelect(prompt)}
          tabIndex={0}
        >
          {prompt}
        </button>
      ))}
    </div>
  );
};

export default SuggestionPrompts; 