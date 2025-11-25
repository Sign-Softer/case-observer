import StepCard from './StepCard';

const steps = [
  {
    stepNumber: 1,
    title: 'Sign Up',
    description: 'Create your free account in less than a minute. No credit card required.',
  },
  {
    stepNumber: 2,
    title: 'Add Cases',
    description: 'Enter case numbers and court institutions. Our system fetches the latest information automatically.',
  },
  {
    stepNumber: 3,
    title: 'Configure Alerts',
    description: 'Choose what updates you want to receive and how often you want to be notified.',
  },
  {
    stepNumber: 4,
    title: 'Stay Informed',
    description: 'Receive instant notifications when your cases are updated. Never miss an important change.',
  },
];

export default function HowItWorks() {
  return (
    <section id="how-it-works" className="py-12 sm:py-16 md:py-20 bg-white">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="text-center mb-12 sm:mb-16">
          <h2 className="text-2xl sm:text-3xl md:text-4xl font-bold text-gray-900 mb-3 sm:mb-4">
            How It Works
          </h2>
          <p className="text-base sm:text-lg text-gray-600 max-w-2xl mx-auto px-4">
            Get started in minutes. No technical knowledge required.
          </p>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6 sm:gap-8">
          {steps.map((step) => (
            <StepCard
              key={step.stepNumber}
              stepNumber={step.stepNumber}
              title={step.title}
              description={step.description}
            />
          ))}
        </div>
      </div>
    </section>
  );
}

