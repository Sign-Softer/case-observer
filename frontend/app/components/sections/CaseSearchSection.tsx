import CaseSearch from '../CaseSearch';

export default function CaseSearchSection() {
  return (
    <section className="py-12 sm:py-16 bg-white">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="text-center mb-8 sm:mb-12">
          <h2 className="text-2xl sm:text-3xl md:text-4xl font-bold text-gray-900 mb-3 sm:mb-4">
            Try It Now - Search Any Case
          </h2>
          <p className="text-base sm:text-lg text-gray-600 max-w-2xl mx-auto px-4">
            Enter a case number and court institution to see real-time case information. No account required for searching.
          </p>
        </div>
        <CaseSearch />
      </div>
    </section>
  );
}

