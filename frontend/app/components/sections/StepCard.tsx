interface StepCardProps {
  stepNumber: number;
  title: string;
  description: string;
}

export default function StepCard({ stepNumber, title, description }: StepCardProps) {
  return (
    <div className="text-center">
      <div className="w-12 h-12 sm:w-16 sm:h-16 bg-blue-600 text-white rounded-full flex items-center justify-center text-xl sm:text-2xl font-bold mx-auto mb-3 sm:mb-4">
        {stepNumber}
      </div>
      <h3 className="text-lg sm:text-xl font-semibold text-gray-900 mb-2">{title}</h3>
      <p className="text-sm sm:text-base text-gray-600 px-2">{description}</p>
    </div>
  );
}

