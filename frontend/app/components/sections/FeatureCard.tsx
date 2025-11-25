interface FeatureCardProps {
  icon: React.ReactNode;
  title: string;
  description: string;
  iconBgColor: string;
  iconColor: string;
}

export default function FeatureCard({ icon, title, description, iconBgColor, iconColor }: FeatureCardProps) {
  return (
    <div className="bg-white p-6 sm:p-8 rounded-lg shadow-md hover:shadow-lg transition-shadow">
      <div className={`w-10 h-10 sm:w-12 sm:h-12 ${iconBgColor} rounded-lg flex items-center justify-center mb-4`}>
        <div className={iconColor}>
          {icon}
        </div>
      </div>
      <h3 className="text-lg sm:text-xl font-semibold text-gray-900 mb-2">{title}</h3>
      <p className="text-sm sm:text-base text-gray-600 leading-relaxed">{description}</p>
    </div>
  );
}

