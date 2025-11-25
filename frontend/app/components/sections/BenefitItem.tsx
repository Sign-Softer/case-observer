interface BenefitItemProps {
  title: string;
  description: string;
}

export default function BenefitItem({ title, description }: BenefitItemProps) {
  return (
    <div className="flex items-start gap-3 sm:gap-4">
      <div className="flex-shrink-0 w-5 h-5 sm:w-6 sm:h-6 bg-green-500 rounded-full flex items-center justify-center mt-1">
        <svg className="w-3 h-3 sm:w-4 sm:h-4 text-white" fill="currentColor" viewBox="0 0 20 20">
          <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
        </svg>
      </div>
      <div>
        <h3 className="font-semibold text-sm sm:text-base text-gray-900 mb-1">{title}</h3>
        <p className="text-xs sm:text-sm text-gray-600">{description}</p>
      </div>
    </div>
  );
}

