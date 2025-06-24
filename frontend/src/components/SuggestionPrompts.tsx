import React from 'react';

interface SuggestionPromptsProps {
  onSelect: (prompt: string) => void;
}

const suggestions = [
  'Itinerary for a 4 day trip to Vegas',
  'Suggest places to visit in Japan',
  'Best restaurants in New York',
];

const SuggestionPrompts: React.FC<SuggestionPromptsProps> = ({ onSelect }) => {
  return (
    <div className="absolute left-1/2 -translate-x-1/2 bottom-28 z-20 flex gap-2 pointer-events-none select-none">
      {suggestions.map((prompt, idx) => (
        <button
          key={idx}
          className="bg-gray-100 text-gray-600 px-2 py-2 rounded-full shadow-sm border border-gray-200 hover:bg-gray-200 transition pointer-events-auto select-auto text-xs font-small"
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